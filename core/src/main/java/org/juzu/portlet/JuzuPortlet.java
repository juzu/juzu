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

import org.juzu.impl.application.ApplicationBootstrap;
import org.juzu.impl.application.ApplicationException;
import org.juzu.impl.model.processor.MainProcessor;
import org.juzu.impl.spi.fs.classloader.ClassLoaderFileSystem;
import org.juzu.impl.spi.inject.InjectBootstrap;
import org.juzu.impl.spi.inject.cdi.CDIBootstrap;
import org.juzu.impl.spi.inject.spring.SpringBootstrap;
import org.juzu.impl.spi.request.portlet.PortletActionBridge;
import org.juzu.impl.spi.request.portlet.PortletRenderBridge;
import org.juzu.impl.spi.request.portlet.PortletResourceBridge;
import org.juzu.impl.utils.Tools;
import org.juzu.impl.utils.TrimmingException;
import org.juzu.metadata.ApplicationDescriptor;
import org.juzu.impl.application.InternalApplicationContext;
import org.juzu.impl.compiler.CompilationError;
import org.juzu.impl.compiler.Compiler;
import org.juzu.impl.fs.Change;
import org.juzu.impl.fs.FileSystemScanner;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.jar.JarFileSystem;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.war.WarFileSystem;
import org.juzu.impl.utils.DevClassLoader;
import org.w3c.dom.Element;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.MimeResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceServingPortlet;
import javax.portlet.ResourceURL;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
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

   /** The jars in WEB-INF/lib . */
   private List<URL> jarURLs;

   /** . */
   private ClassLoaderFileSystem classLoaderFS;

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
                  System.out.println("[" + config.getPortletName() + "] Detected changes : " + changes);
                  applicationContext = null;
               }
               else
               {
                  System.out.println("[" + config.getPortletName() + "] No changes detected");
               }
            }

            //
            if (applicationContext == null)
            {
               System.out.println("[" + config.getPortletName() + "] Building application");

               // We load it once as it is an expensive resource
               if (classLoaderFS == null)
               {
                  ClassLoader devCL = new DevClassLoader(Thread.currentThread().getContextClassLoader());
                  classLoaderFS = new ClassLoaderFileSystem(devCL);
               }

               //
               WarFileSystem fs = WarFileSystem.create(config.getPortletContext(), "/WEB-INF/src/");
               RAMFileSystem classes = new RAMFileSystem();

               //
               Compiler compiler = new Compiler(fs, classLoaderFS, classes, classes);
               compiler.addAnnotationProcessor(new MainProcessor());
               List<CompilationError> res = compiler.compile();
               if (res.isEmpty())
               {
                  ClassLoader cl2 = new URLClassLoader(new URL[]{classes.getURL()}, classLoaderFS.getClassLoader());
                  boot(classes, cl2);
                  devScanner = new FileSystemScanner<String>(fs);
                  devScanner.scan();
                  System.out.println("[" + config.getPortletName() + "] Dev mode scanner monitoring " + fs.getFile(fs.getRoot()));
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
      P f = classes.getPath(Arrays.asList("org", "juzu", "config.properties"));
      URL url = classes.getURL(f);
      InputStream in = url.openStream();
      Properties props = new Properties();
      props.load(in);

      // Get the application name
      String appName = config.getInitParameter("juzu.app_name");
      String fqn = null;
      if (appName != null)
      {
         fqn = props.getProperty(appName.trim());
      }
      else
      {
         // Find the first valid application for now
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
      }

      //
      if (fqn == null)
      {
         throw new Exception("Could not find an application to start " + props);
      }

      //
      Class<?> clazz = cl.loadClass(fqn);
      Field field = clazz.getDeclaredField("DESCRIPTOR");
      ApplicationDescriptor descriptor = (ApplicationDescriptor)field.get(null);

      // Find the juzu jar
      URL mainURL = null;
      for (URL jarURL : jarURLs)
      {
         URL configURL = new URL("jar:" + jarURL.toString() + "!/org/juzu/impl/application/ApplicationBootstrap.class");
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
      JarFileSystem libs = new JarFileSystem(new JarFile(new File(mainURL.toURI())));

      //
      String inject = config.getInitParameter("juzu.inject");
      InjectBootstrap injectBootstrap;
      if (inject == null)
      {
         inject = "weld";
      }
      inject = inject.trim().toLowerCase();
      if ("weld".equals(inject))
      {
      injectBootstrap = new CDIBootstrap();
      }
      else if ("spring".equals(inject))
      {
         injectBootstrap = new SpringBootstrap();
      }
      else
      {
         throw new PortletException("unrecognized inject vendor " + inject);
      }
      System.out.println("[" + config.getPortletName() + "] Using injection " + injectBootstrap.getClass().getName());

      //
      injectBootstrap.addFileSystem(classes);
      injectBootstrap.addFileSystem(libs);
      injectBootstrap.setClassLoader(cl);

      //
      if (injectBootstrap instanceof SpringBootstrap)
      {
         URL configurationURL = config.getPortletContext().getResource("/WEB-INF/spring.xml");
         if (configurationURL != null)
         {
            ((SpringBootstrap)injectBootstrap).setConfigurationURL(configurationURL);
         }
      }

      //
      ApplicationBootstrap bootstrap = new ApplicationBootstrap(
         injectBootstrap,
         descriptor
      );

      //
      System.out.println("[" + config.getPortletName() + "] Starting " + descriptor.getName());
      bootstrap.start();
      applicationContext = bootstrap.getContext();
   }

   public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException
   {
      try
      {
         applicationContext.invoke(new PortletActionBridge(request, response));
      }
      catch (ApplicationException e)
      {
         // For now we do that until we find something better specially for the dev mode
         throw new PortletException(e.getCause());
      }
   }

   /**
    * Purge the session.
    *
    * @param req the request owning the session
    */
   private void purgeSession(PortletRequest req)
   {
      PortletSession session = req.getPortletSession();
      for (String key : new HashSet<String>(session.getAttributeMap().keySet()))
      {
         session.removeAttribute(key);
      }
   }

   public void render(final RenderRequest request, final RenderResponse response) throws PortletException, IOException
   {
      Collection<CompilationError> errors = boot();

      //
      if (errors == null || errors.isEmpty())
      {
         if (errors != null)
         {
            purgeSession(request);
         }

         //
         try
         {
            TrimmingException.invoke(new TrimmingException.Callback()
            {
               public void call() throws Throwable
               {
                  try
                  {
                     PortletRenderBridge bridge = new PortletRenderBridge(request, response, !prod);
                     applicationContext.invoke(bridge);
                     bridge.commit();
                  }
                  catch (ApplicationException e)
                  {
                     throw e.getCause();
                  }
               }
            });
         }
         catch (TrimmingException e)
         {
            if (prod)
            {
               throw new PortletException(e.getSource());
            }
            else
            {
               renderException(request, response, e);
            }
         }
         finally
         {
            // Clean up flash scope
            PortletSession session = request.getPortletSession(false);
            if (session != null)
            {
               session.removeAttribute("org.juzu.flash_scope");
            }
         }
      }
      else
      {
         renderErrors(request, response, errors);
      }
   }
   
   public void serveResource(final ResourceRequest request, final ResourceResponse response) throws PortletException, IOException
   {
      String resourceId = request.getResourceID();
      if ("css".equals(resourceId))
      {
         InputStream in = JuzuPortlet.class.getResourceAsStream("juzu.css");
         response.setContentType("text/css");
         Tools.copy(in, response.getPortletOutputStream());
      }
      else
      {
         Collection<CompilationError> errors = boot();

         //
         if (errors == null || errors.isEmpty())
         {
            if (errors != null)
            {
               purgeSession(request);
            }

            //
            try
            {
               TrimmingException.invoke(new TrimmingException.Callback()
               {
                  public void call() throws Throwable
                  {
                     try
                     {
                        PortletResourceBridge bridge = new PortletResourceBridge(request, response, !prod);
                        applicationContext.invoke(bridge);
                        bridge.commit();
                     }
                     catch (ApplicationException e)
                     {
                        throw e.getCause();
                     }
                  }
               });
            }
            catch (TrimmingException e)
            {
               if (prod)
               {
                  throw new PortletException(e.getSource());
               }
               else
               {
                  renderException(request, response, e);
               }
            }
         }
         else
         {
            renderErrors(request, response, errors);
         }
      }
   }

   private void sendJuzuCSS(MimeResponse resp)
   {
      ResourceURL url = resp.createResourceURL();
      url.setResourceID("css");
      Element linkElt = resp.createElement("link");
      linkElt.setAttribute("rel", "stylesheet");
      linkElt.setAttribute("type", "text/css");
      linkElt.setAttribute("media", "screen");
      linkElt.setAttribute("href", url.toString());
      resp.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, linkElt);
   }

   private void renderException(PortletRequest req, MimeResponse resp, Throwable t) throws PortletException, IOException
   {
      // Trim the stack trace to remove stuff we don't want to see
      int size = 0;
      StackTraceElement[] trace = t.getStackTrace();
      for (StackTraceElement element : trace)
      {
         if (element.getClassName().equals(JuzuPortlet.class.getName()))
         {
            break;
         }
         else
         {
            size++;
         }
      }
      StackTraceElement[] ourTrace = new StackTraceElement[size];
      System.arraycopy(trace, 0, ourTrace, 0, ourTrace.length);
      t.setStackTrace(ourTrace);

      //
      sendJuzuCSS(resp);

      //
      final PrintWriter writer = resp.getWriter();

      // We hack a bit
      final AtomicBoolean open = new AtomicBoolean(false);
      PrintWriter formatter = new PrintWriter(writer)
      {
         @Override
         public void println(Object x)
         {
            if (open.get())
            {
               super.append("</ul></pre>");
            }
            super.append("<div class=\"juzu-message\">");
            super.append(String.valueOf(x));
            super.append("</div>");
            open.set(false);
         }

         @Override
         public void println(String x)
         {
            if (!open.get())
            {
               super.append("<pre><ul>");
               open.set(true);
            }
            super.append("<li><span>");
            super.append(x);
            super.append("</span></li>");
         }

         @Override
         public void println()
         {
            // Do nothing
         }
      };

      //
      writer.append("<div class=\"juzu\">");
      writer.append("<div class=\"juzu-box\">");

      // We hack a bit with our formatter
      t.printStackTrace(formatter);

      //
      if (open.get())
      {
         writer.append("</ul></pre>");
      }
      
      //
      writer.append("</div>");
      writer.append("</div>");
   }

   private void renderErrors(PortletRequest req, MimeResponse resp, Collection<CompilationError> errors) throws PortletException, IOException
   {
      sendJuzuCSS(resp);

      //
      PrintWriter writer = resp.getWriter();
         
      //
      writer.append("<div class=\"juzu\">");
      for (CompilationError error : errors)
      {
         writer.append("<div class=\"juzu-box\">");
         writer.append("<div class=\"juzu-message\">").append(error.getMessage()).append("</div>");

         // Display the source code
         File source = error.getSourceFile();
         if (source != null)
         {
            int line = error.getLocation().getLine();
            int from = line - 2;
            int to = line + 3;
            BufferedReader reader = new BufferedReader(new FileReader(source));
            int count = 1;
            writer.append("<pre><ol start=\"").append(String.valueOf(from)).append("\">");
            for (String s = reader.readLine();s != null;s = reader.readLine())
            {
               if (count >= from && count < to)
               {
                  if (count == line)
                  {
                     writer.append("<li><span class=\"error\">").append(s).append("</span></li>");
                  }
                  else
                  {
                     writer.append("<li><span>").append(s).append("</span></li>");
                  }
               }
               count++;
            }
            writer.append("</ol></pre>");
         }
         writer.append("</div>");
      }
      writer.append("</div>");
   }

   public void destroy()
   {
   }
}
