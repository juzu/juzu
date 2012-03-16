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

import org.juzu.PropertyType;
import org.juzu.impl.application.ApplicationException;
import org.juzu.impl.application.ApplicationRuntime;
import org.juzu.impl.asset.Server;
import org.juzu.impl.spi.inject.InjectImplementation;
import org.juzu.impl.spi.request.portlet.PortletActionBridge;
import org.juzu.impl.spi.request.portlet.PortletRenderBridge;
import org.juzu.impl.spi.request.portlet.PortletResourceBridge;
import org.juzu.impl.utils.Logger;
import org.juzu.impl.utils.Tools;
import org.juzu.impl.utils.TrimmingException;
import org.juzu.impl.compiler.CompilationError;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.impl.spi.fs.war.WarFileSystem;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceServingPortlet;
import javax.portlet.WindowState;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JuzuPortlet implements Portlet, ResourceServingPortlet
{

   /** . */
   public static final class PORTLET_MODE extends PropertyType<PortletMode> {}

   /** . */
   public static final class WINDOW_STATE extends PropertyType<WindowState> {}

   /** . */
   public static final PORTLET_MODE PORTLET_MODE = new PORTLET_MODE();

   /** . */
   public static final WINDOW_STATE WINDOW_STATE = new WINDOW_STATE();

   /** . */
   private PortletConfig config;

   /** . */
   private ApplicationRuntime<?, String, String> runtime;

   /** . */
   private boolean prod;

   /** . */
   private String srcPath;

   /** . */
   private String appName;

   /** . */
   private InjectImplementation injectImpl;

   /** . */
   private ReadFileSystem<String> libs;

   /** . */
   private ReadFileSystem<String> resources;

   /** . */
   private Logger log;
   
   public void init(final PortletConfig config) throws PortletException
   {
      this.config = config;
      this.log = new Logger()
      {
         public void log(CharSequence msg)
         {
            System.out.println("[" + config.getPortletName() + "] " + msg);
         }
         public void log(CharSequence msg, Throwable t)
         {
            System.err.println("[" + config.getPortletName() + "] " + msg);
            t.printStackTrace();
         }
      };

      //
      String runMode = config.getInitParameter("juzu.run_mode");
      runMode = runMode == null ? "prod" : runMode.trim().toLowerCase();

      //
      String inject = config.getInitParameter("juzu.inject");
      InjectImplementation injectImpl;
      if (inject == null)
      {
         injectImpl = InjectImplementation.CDI_WELD;
      }
      else
      {
         inject = inject.trim().toLowerCase();
         if ("weld".equals(inject))
         {
            injectImpl = InjectImplementation.CDI_WELD;
         }
         else if ("spring".equals(inject))
         {
            injectImpl = InjectImplementation.INJECT_SPRING;
         }
         else
         {
            throw new PortletException("unrecognized inject vendor " + inject);
         }
      }
      log.log("Using injection " + injectImpl.name());

      //
      this.appName = config.getInitParameter("juzu.app_name");
      this.prod = !("dev".equals(runMode));
      this.srcPath = config.getInitParameter("juzu.src_path");
      this.injectImpl = injectImpl;
      this.libs = WarFileSystem.create(config.getPortletContext(), "/WEB-INF/lib/");
      this.resources = WarFileSystem.create(config.getPortletContext(), "/WEB-INF/");

      //
      Collection<CompilationError> errors = boot();
      if (errors != null && errors.size() > 0)
      {
         log.log("Error when compiling application " + errors);
      }
   }

   private Collection<CompilationError> boot() throws PortletException
   {
      if (runtime == null)
      {
         if (prod)
         {
            runtime = new ApplicationRuntime.Static<String, String, String>(log);
            ((ApplicationRuntime.Static<String, String, String>)runtime).setClasses(WarFileSystem.create(config.getPortletContext(), "/WEB-INF/classes/"));
            ((ApplicationRuntime.Static<String, String, String>)runtime).setClassLoader(Thread.currentThread().getContextClassLoader());
         }
         else
         {
            try
            {
               runtime = new ApplicationRuntime.Dynamic<String, String, String>(log);
               if (srcPath != null)
               {
                  ReadFileSystem<File> fss = new DiskFileSystem(new File(srcPath));
                  ((ApplicationRuntime.Dynamic<String, String, File>)runtime).init(Thread.currentThread().getContextClassLoader(), fss);
               }
               else
               {
                  ReadFileSystem<String> fss = WarFileSystem.create(config.getPortletContext(), "/WEB-INF/src/");
                  ((ApplicationRuntime.Dynamic<String, String, String>)runtime).init(Thread.currentThread().getContextClassLoader(), fss);
               }
            }
            catch (Exception e)
            {
               throw e instanceof PortletException ? (PortletException)e : new PortletException(e);
            }
         }

         //
         Server server = (Server)config.getPortletContext().getAttribute("asset.server");

         //
         runtime.setLibs(libs);
         runtime.setResources(resources);
         runtime.setInjectImplementation(injectImpl);
         runtime.setName(appName);
         runtime.setAssetServer(server);
      }

      //
      try
      {
         return runtime.boot();
      }
      catch (Exception e)
      {
         throw new PortletException("Could not find an application to start", e);
      }
   }

   public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException
   {
      PortletActionBridge bridge = new PortletActionBridge(request, response);
      try
      {
         runtime.getContext().invoke(bridge);
      }
      catch (ApplicationException e)
      {
         // For now we do that until we find something better specially for the dev mode
         throw new PortletException(e.getCause());
      }
      finally
      {
         bridge.close();
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
                  PortletRenderBridge bridge = new PortletRenderBridge(request, response, !prod);
                  try
                  {
                     runtime.getContext().invoke(bridge);
                     bridge.commit();
                  }
                  catch (ApplicationException e)
                  {
                     throw e.getCause();
                  }
                  finally
                  {
                     bridge.close();
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
               renderThrowable(response.getWriter(), e);
            }
         }
      }
      else
      {
         renderErrors(response.getWriter(), errors);
      }
   }
   
   public void serveResource(final ResourceRequest request, final ResourceResponse response) throws PortletException, IOException
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
                  PortletResourceBridge bridge = new PortletResourceBridge(request, response, !prod);
                  try
                  {
                     runtime.getContext().invoke(bridge);
                     bridge.commit();
                  }
                  catch (ApplicationException e)
                  {
                     throw e.getCause();
                  }
                  finally
                  {
                     bridge.close();
                  }
               }
            });
         }
         catch (TrimmingException e)
         {
            // Internal server error
            response.setProperty(ResourceResponse.HTTP_STATUS_CODE, "500");

            //
            logThrowable(e);

            //
            if (!prod)
            {
               PrintWriter writer = response.getWriter();
               writer.print("<html>\n");
               writer.print("<head>\n");
               writer.print("</head>\n");
               writer.print("<body>\n");
               renderThrowable(writer, e);
               writer.print("</body>\n");
            }
         }
      }
      else
      {
         // Internal Server Error
         response.setProperty(ResourceResponse.HTTP_STATUS_CODE, "500");

         // Log errors
         logErrors(errors);

         //
         if (!prod)
         {
            PrintWriter writer = response.getWriter();
            writer.print("<html>\n");
            writer.print("<head>\n");
            writer.print("</head>\n");
            writer.print("<body>\n");
            renderErrors(writer, errors);
            writer.print("</body>\n");
         }
      }
   }

   private void sendJuzuCSS(PrintWriter writer) throws IOException
   {
      // Get CSS
      URL cssURL = JuzuPortlet.class.getResource("juzu.css");
      String css = Tools.read(cssURL);
      css = css.replace("\"", "\\\"");
      css = css.replace("'", "\\'");
      css = css.replace("\n", "\\n");

      //
      writer.append("<script type='text/javascript'>\n");
      writer.append("var styleElement = document.createElement('style');\n");
      writer.append("var css = '");
      writer.append(css);
      writer.append("';\n");
      writer.append("styleElement.type = 'text/css';\n");
      writer.append("if (styleElement.styleSheet) {;\n");
      writer.append("styleElement.styleSheet.cssText = css;\n");
      writer.append("} else {\n");
      writer.append("styleElement.appendChild(document.createTextNode(css));\n");
      writer.append("}\n");
      writer.append("document.getElementsByTagName(\"head\")[0].appendChild(styleElement);\n");
      writer.append("</script>\n");
   }

   private void logThrowable(Throwable t)
   {
      log.log(t.getMessage(), t);
   }

   private void logErrors(Collection<CompilationError> errors)
   {
      // Todo format that better like it is in renderErrors
      StringBuilder sb = new StringBuilder("Compilation errors:\n");
      for (CompilationError error : errors)
      {
         if (error.getSourceFile() != null)
         {
            sb.append(error.getSourceFile().getAbsolutePath());
         }
         else
         {
            sb.append(error.getSource());
         }
         sb.append(':').append(error.getLocation().getLine()).append(':').append(error.getMessage()).append('\n');
      }
      log.log(sb.toString());
   }

   private void renderThrowable(PrintWriter writer, Throwable t) throws PortletException, IOException
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
      sendJuzuCSS(writer);

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

   private void renderErrors(PrintWriter writer, Collection<CompilationError> errors) throws PortletException, IOException
   {
      sendJuzuCSS(writer);

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
