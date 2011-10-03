package org.juzu.application;

import org.juzu.Resource;
import org.juzu.impl.cdi.Export;
import org.juzu.impl.cdi.InvocationContext;
import org.juzu.impl.cdi.InvocationScoped;
import org.juzu.impl.spi.cdi.Container;
import org.juzu.request.RenderContext;
import org.juzu.request.RequestContext;
import org.juzu.template.Template;
import org.juzu.text.Printer;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Export
@Singleton
public class ApplicationContext
{

   /** . */
   private final ApplicationDescriptor descriptor;

   /** . */
   private final Container container;

   /** . */
   private final ThreadLocal<RequestContext> current = new ThreadLocal<RequestContext>();

   public ApplicationContext()
   {
      Bootstrap bootstrap = Bootstrap.foo.get();

      //
      this.descriptor = bootstrap.descriptor;
      this.container = bootstrap.container;
   }

   public ApplicationDescriptor getDescriptor()
   {
      return descriptor;
   }

   /**
    * For now pretty simple resolution algorithm.
    *
    * @param data the data
    * @return the render descriptor or null if nothing could be resolved
    */
   public ControllerMethod resolve(Map<String, String[]> data)
   {
      List<ControllerMethod> renders = descriptor.getControllerMethods();
      return renders.isEmpty() ? null : renders.get(0);
   }


   public void invoke(RequestContext context)
   {
      try
      {
         current.set(context);
         InvocationContext.start();

         //
         if (context instanceof RenderContext)
         {
            invoke((RenderContext)context);
         }
         else
         {
            throw new UnsupportedOperationException();
         }
      }
      finally
      {
         current.set(null);
         InvocationContext.stop();
      }
   }


   private void invoke(RenderContext renderContext)
   {
      ControllerMethod method = resolve(renderContext.getParameters());

      //
      if (method == null)
      {
         throw new UnsupportedOperationException("handle me gracefully");
      }
      else
      {
         Class<?> type = method.getType();
         BeanManager mgr = container.getManager();
         Set<? extends Bean> beans = mgr.getBeans(type);

         if (beans.size() == 1)
         {
            try
            {
               // Get the bean
               Bean bean = beans.iterator().next();
               CreationalContext<?> cc = mgr.createCreationalContext(bean);
               Object o = mgr.getReference(bean, type, cc);

               // For now we do only zero arg invocations
               method.getMethod().invoke(o);
            }
            catch (Exception e)
            {
               throw new UnsupportedOperationException("handle me gracefully", e);
            }
         }
      }
   }

   @Produces
   @InvocationScoped
   public Printer getPrinter()
   {
      RequestContext context = current.get();
      if (context instanceof RenderContext)
      {
         return ((RenderContext)context).getPrinter();
      }
      else
      {
         throw new UnsupportedOperationException("handle me gracefully");
      }
   }

   @Produces
   public Template getRenderer(InjectionPoint point)
   {
      Bean<?> bean = point.getBean();
      Resource template = point.getAnnotated().getAnnotation(Resource.class);
      StringBuilder id = new StringBuilder(descriptor.getTemplatesPackageName());
      if (id.length() > 0)
      {
         id.append('.');
      }
      id.append(template.value(), 0, template.value().indexOf('.'));
      return new Template(id.toString());
   }
}
