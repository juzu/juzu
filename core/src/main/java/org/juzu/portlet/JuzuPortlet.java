package org.juzu.portlet;

import org.juzu.impl.application.ApplicationContext;
import org.juzu.application.ApplicationDescriptor;
import org.juzu.impl.application.Bootstrap;
import org.juzu.impl.application.ApplicationProcessor;
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
import org.juzu.impl.template.TemplateProcessor;
import org.juzu.impl.utils.DevClassLoader;
import org.juzu.impl.request.ActionContext;
import org.juzu.impl.request.RenderContext;
import org.juzu.text.Printer;
import org.juzu.text.WriterPrinter;

import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JuzuPortlet implements Portlet
{

   /** . */
   private ApplicationContext applicationContext;

   /** . */
   private boolean prod;

   /** . */
   private PortletConfig config;

   /** . */
   private FileSystemScanner<String> devScanner;

   public void init(PortletConfig config) throws PortletException
   {
      String runMode = config.getInitParameter("juzu.run_mode");
      runMode = runMode == null ? "prod" : runMode.trim().toLowerCase();

      //
      this.config = config;
      this.prod = !("dev".equals(runMode));

      //
      Collection<CompilationError> errors = boot();
      if (errors.size() > 0)
      {
         System.out.println("Error when compiling application " + errors);
      }
   }

   private Collection<CompilationError> boot() throws PortletException
   {
      long t = -System.currentTimeMillis();
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
               throw new PortletException("Could not find an application to start");
            }
         }
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
                  applicationContext = null;
               }
            }

            //
            if (applicationContext == null)
            {
               System.out.println("Building dev application");

               //
               List<URL> classPath = new ArrayList<URL>();
               classPath.add(Inject.class.getProtectionDomain().getCodeSource().getLocation());
               classPath.add(Bean.class.getProtectionDomain().getCodeSource().getLocation());
               classPath.add(JuzuPortlet.class.getProtectionDomain().getCodeSource().getLocation());

               //
               WarFileSystem fs = WarFileSystem.create(config.getPortletContext(), "/WEB-INF/src/");
               RAMFileSystem classes = new RAMFileSystem();

               //
               Compiler<String, RAMPath> compiler = new Compiler<String, RAMPath>(classPath, fs, classes);
               compiler.addAnnotationProcessor(new TemplateProcessor());
               compiler.addAnnotationProcessor(new ApplicationProcessor());
               List<CompilationError> res = compiler.compile();
               if (res.isEmpty())
               {
                  ClassLoader cl1 = new DevClassLoader(Thread.currentThread().getContextClassLoader());
                  ClassLoader cl2 = new URLClassLoader(new URL[]{classes.getURL()}, cl1);
                  boot(classes, cl2);
                  devScanner = new FileSystemScanner<String>(fs);
                  devScanner.scan();
               }
               else
               {
                  return res;
               }
            }
         }
         catch (Exception e)
         {
            throw new PortletException(e);
         }
      }

      //
      t += System.currentTimeMillis();
      System.out.println("Booted in " + t + " ms");
      return Collections.emptyList();
   }

   private <P, D> void boot(ReadFileSystem<P> classes, ClassLoader cl) throws Exception
   {
      // Find an application
      P f = classes.getFile(Arrays.asList("org", "juzu"), "config.properties");
      URL url = classes.getURL(f);
      InputStream in = url.openStream();
      Properties props = new Properties();
      props.load(in);

      //
      if (props.size() != 1)
      {
         throw new Exception("Could not find an application to start " + props);
      }
      Map.Entry<Object, Object> entry = props.entrySet().iterator().next();

      //
      String fqn = entry.getValue().toString();
      System.out.println("loading class descriptor " + fqn);
      Class<?> clazz = cl.loadClass(fqn);
      Field field = clazz.getDeclaredField("DESCRIPTOR");
      ApplicationDescriptor descriptor = (ApplicationDescriptor)field.get(null);

      //
      JarFileSystem libs = new JarFileSystem(new JarFile(new File(Bootstrap.class.getProtectionDomain().getCodeSource().getLocation().toURI())));

      //
      Container container = new org.juzu.impl.spi.cdi.weld.WeldContainer(cl);
      container.addFileSystem(classes);
      container.addFileSystem(libs);

      //
      System.out.println("Starting application [" + descriptor.getName() + "]");
      Bootstrap boot = new Bootstrap(container, descriptor);
      boot.start();
      applicationContext = boot.getContext();
   }

   public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException
   {
      ActionContext actionContext = new ActionContext(request.getParameterMap());

      //
      applicationContext.invoke(actionContext);
   }

   public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException
   {
      Collection<CompilationError> errors = boot();

      //
      if (errors.isEmpty())
      {
         //
         Printer printer = new WriterPrinter(response.getWriter());

         //
         RenderContext renderContext = new RenderContext(
            request.getParameterMap(),
            printer,
            new PortletURLBuilderContext(response)
         );

         //
         applicationContext.invoke(renderContext);
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
