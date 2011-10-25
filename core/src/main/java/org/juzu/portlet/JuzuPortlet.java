/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.juzu.portlet;

import org.juzu.impl.processing.MainProcessor;
import org.juzu.impl.spi.request.portlet.PortletActionBridge;
import org.juzu.impl.spi.request.portlet.PortletRenderBridge;
import org.juzu.impl.spi.request.portlet.PortletResourceBridge;
import org.juzu.metadata.ApplicationDescriptor;
import org.juzu.impl.application.InternalApplicationContext;
import org.juzu.impl.application.Bootstrap;
import org.juzu.impl.compiler.CompilationError;
import org.juzu.impl.compiler.Compiler;
import org.juzu.impl.fs.Change;
import org.juzu.impl.fs.FileSystemScanner;
import org.juzu.impl.spi.cdi.Container;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.jar.JarFileSystem;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.impl.spi.fs.war.WarFileSystem;
import org.juzu.impl.utils.DevClassLoader;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceServingPortlet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JuzuPortlet implements Portlet, ResourceServingPortlet
{

   /** . */
   private InternalApplicationContext applicationContext;

   /** . */
   private boolean prod;

   /** . */
   private PortletConfig config;

   /** . */
   private FileSystemScanner<String> devScanner;

   /** . */
   private ClassLoader classLoader;

   /** The jars in WEB-INF/lib . */
   private List<URL> jarURLs;

   public void init(PortletConfig config) throws PortletException
   {
      try
      {
         String runMode = config.getInitParameter("juzu.run_mode");
         runMode = runMode == null ? "prod" : runMode.trim().toLowerCase();

         //
         List<URL> jars = new ArrayList<URL>();
         WarFileSystem bah = WarFileSystem.create(config.getPortletContext(), "/WEB-INF/lib/");
         for (Iterator<String> i = bah.getChildren(bah.getRoot());i.hasNext();)
         {
            String s = i.next();
            URL url = bah.getURL(s);
            jars.add(url);
         }

         //
         this.config = config;
         this.prod = !("dev".equals(runMode));
         this.jarURLs = jars;

         //
         Collection<CompilationError> errors = boot();
         if (errors != null && errors.size() > 0)
         {
            System.out.println("Error when compiling application " + errors);
         }
      }
      catch (IOException e)
      {
         throw new PortletException(e);
      }
   }

   private Collection<CompilationError> boot() throws PortletException
   {
      if (prod)
      {
         if (applicationContext == null)
         {
            try
            {
               WarFileSystem fs = WarFileSystem.create(config.getPortletContext(), "/WEB-INF/classes/");
               ClassLoader cl = Thread.currentThread().getContextClassLoader();
               boot(fs, cl);
            }
            catch (Exception e)
            {
               throw new PortletException("Could not find an application to start", e);
            }
         }
         return null;
      }
      else
      {
         try
         {
            if (devScanner != null)
            {
               Map<String, Change> changes =  devScanner.scan();
               if (changes.size() > 0)
               {
                  System.out.println("Detected changes : " + changes);
                  applicationContext = null;
               }
            }

            //
            if (applicationContext == null)
            {
               System.out.println("Building application");

               // Build the classpath
               List<URL> classPath = new ArrayList<URL>();
               for (URL jarURL : jarURLs)
               {
                  URL configURL = new URL("jar:" + jarURL.toString() + "!/org/juzu/config.properties");
                  try
                  {
                     configURL.openStream();
                  }
                  catch (IOException e)
                  {
                     classPath.add(jarURL);
                  }
               }

               //
               WarFileSystem fs = WarFileSystem.create(config.getPortletContext(), "/WEB-INF/src/");
               RAMFileSystem classes = new RAMFileSystem();

               //
               Compiler<String, RAMPath> compiler = new Compiler<String, RAMPath>(classPath, fs, classes);
               compiler.addAnnotationProcessor(new MainProcessor());
               List<CompilationError> res = compiler.compile();
               if (res.isEmpty())
               {
                  ClassLoader cl1 = new DevClassLoader(Thread.currentThread().getContextClassLoader());
                  ClassLoader cl2 = new URLClassLoader(new URL[]{classes.getURL()}, cl1);
                  boot(classes, cl2);
                  devScanner = new FileSystemScanner<String>(fs);
                  devScanner.scan();
                  return Collections.emptyList();
               }
               else
               {
                  return res;
               }
            }
            else
            {
               return null;
            }
         }
         catch (Exception e)
         {
            throw e instanceof PortletException ? (PortletException)e : new PortletException(e);
         }
      }
   }

   private <P, D> void boot(ReadFileSystem<P> classes, ClassLoader cl) throws Exception
   {
      // Find an application
      P f = classes.getFile(Arrays.asList("org", "juzu"), "config.properties");
      URL url = classes.getURL(f);
      InputStream in = url.openStream();
      Properties props = new Properties();
      props.load(in);

      // Find the first valid application for now
      String fqn = null;
      for (Map.Entry<Object, Object> entry : props.entrySet())
      {
         String a = entry.getKey().toString();
         String b = entry.getValue().toString();
         if (a.length() > 0 && b.length() > 0)
         {
            fqn = b;
            break;
         }
      }
      if (fqn == null)
      {
         throw new Exception("Could not find an application to start " + props);
      }

      //
      System.out.println("loading class descriptor " + fqn);
      Class<?> clazz = cl.loadClass(fqn);
      Field field = clazz.getDeclaredField("DESCRIPTOR");
      ApplicationDescriptor descriptor = (ApplicationDescriptor)field.get(null);

      // Find the juzu jar
      URL mainURL = null;
      for (URL jarURL : jarURLs)
      {
         URL configURL = new URL("jar:" + jarURL.toString() + "!/org/juzu/impl/application/Bootstrap.class");
         try
         {
            configURL.openStream();
            mainURL = jarURL;
            break;
         }
         catch (IOException ignore)
         {
         }
      }
      if (mainURL == null)
      {
         throw new PortletException("Cannot find juzu jar among " + jarURLs);
      }
      System.out.println("mainURL = " + mainURL);
      JarFileSystem libs = new JarFileSystem(new JarFile(new File(mainURL.toURI())));

      //
      Container container = new org.juzu.impl.spi.cdi.weld.WeldContainer(cl);
      container.addFileSystem(classes);
      container.addFileSystem(libs);

      //
      System.out.println("Starting application [" + descriptor.getName() + "]");
      Bootstrap boot = new Bootstrap(container, descriptor);
      boot.start();
      applicationContext = boot.getContext();
      classLoader = cl;
   }

   public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException
   {
      applicationContext.invoke(new PortletActionBridge(request, response));
   }

   public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException
   {
      Collection<CompilationError> errors = boot();

      //
      if (errors == null || errors.isEmpty())
      {
         if (errors != null)
         {
            request.getPortletSession().invalidate();
         }

         //
         applicationContext.invoke(new PortletRenderBridge(request, response));

         // Clean up flash scope
         PortletSession session = request.getPortletSession(false);
         if (session != null)
         {
            session.removeAttribute("org.juzu.flash_scope");
         }
      }
      else
      {
//         Element linkElt = response.createElement("link");
//         linkElt.setAttribute("rel", "stylesheet");
//         linkElt.setAttribute("href", "http://twitter.github.com/bootstrap/1.3.0/bootstrap.min.css");
//         response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, linkElt);

         // Basic error reporting for now
         StringBuilder sb = new StringBuilder();
         for (CompilationError error : errors)
         {

            String at = error.getSource();

            //
            sb.append("<p>");
            sb.append("<div>Compilation error at ").append(at).append(" ").append(error.getLocation()).append("</div>");
            sb.append("<div>");
            sb.append(error.getMessage());
            sb.append("</div>");
            sb.append("</p>");
         }
         response.getWriter().print(sb);
      }
   }

   public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException
   {
      Collection<CompilationError> errors = boot();

      //
      if (errors == null || errors.isEmpty())
      {
         if (errors != null)
         {
            request.getPortletSession().invalidate();
         }

         //
         applicationContext.invoke(new PortletResourceBridge(request, response));
      }
      else
      {
//         Element linkElt = response.createElement("link");
//         linkElt.setAttribute("rel", "stylesheet");
//         linkElt.setAttribute("href", "http://twitter.github.com/bootstrap/1.3.0/bootstrap.min.css");
//         response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, linkElt);

         // Basic error reporting for now
         StringBuilder sb = new StringBuilder();
         for (CompilationError error : errors)
         {

            String at = error.getSource();

            //
            sb.append("<p>");
            sb.append("<div>Compilation error at ").append(at).append(" ").append(error.getLocation()).append("</div>");
            sb.append("<div>");
            sb.append(error.getMessage());
            sb.append("</div>");
            sb.append("</p>");
         }
         response.getWriter().print(sb);
      }
   }

   public void destroy()
   {

   }
}
