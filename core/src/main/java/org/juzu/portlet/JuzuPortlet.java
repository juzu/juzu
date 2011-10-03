package org.juzu.portlet;

import org.juzu.application.ApplicationContext;
import org.juzu.application.ApplicationDescriptor;
import org.juzu.application.Bootstrap;
import org.juzu.impl.spi.cdi.Container;
import org.juzu.impl.spi.fs.jar.JarFileSystem;
import org.juzu.impl.spi.fs.war.WarFileSystem;
import org.juzu.request.RenderContext;
import org.juzu.text.Printer;
import org.juzu.text.WriterPrinter;

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
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JuzuPortlet implements Portlet
{

   /** . */
   private ApplicationContext applicationContext;

   public void init(PortletConfig config) throws PortletException
   {
      // Find an application
      Properties props;
      try
      {
         URL url = Thread.currentThread().getContextClassLoader().getResource("org/juzu/config.properties");
         InputStream in = url.openStream();
         props = new Properties();
         props.load(in);
      }
      catch (IOException e)
      {
         throw new PortletException("Could not find an application to start");
      }

      //
      if (props.size() != 1)
      {
         throw new PortletException("Could not find an application to start " + props);
      }
      Map.Entry<Object, Object> entry = props.entrySet().iterator().next();

      //
      ApplicationDescriptor descriptor;
      String fqn = entry.getValue().toString();
      try
      {
         Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(fqn);
         Field f = clazz.getDeclaredField("DESCRIPTOR");
         descriptor = (ApplicationDescriptor)f.get(null);
      }
      catch (Exception e)
      {
         throw new PortletException("Could not find an application to start " + fqn, e);
      }

      //
      Container container;
      try
      {
         URL url = Bootstrap.class.getProtectionDomain().getCodeSource().getLocation();
         File f = new File(url.toURI());
         JarFileSystem jarFS = new JarFileSystem(new JarFile(f));

         //
         WarFileSystem fs = WarFileSystem.create(config.getPortletContext(), "/WEB-INF/classes/");

         //
         container = new org.juzu.impl.spi.cdi.weld.WeldContainer();
         container.addFileSystem(fs);
         container.addFileSystem(jarFS);

         //
         System.out.println("Starting application [" + descriptor.getName() + "]");
         Bootstrap boot = new Bootstrap(container, descriptor);
         boot.start();
         applicationContext = boot.getContext();
      }
      catch (Exception e)
      {
         throw new PortletException("Error when starting application [" + descriptor.getName() + "]", e);
      }
   }

   public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException
   {

      //
      Printer printer = new WriterPrinter(response.getWriter());

      //
      RenderContext renderContext = new RenderContext(
         request.getParameterMap(),
         printer
      );

      //
      applicationContext.invoke(renderContext);
   }

   public void destroy()
   {

   }
}
