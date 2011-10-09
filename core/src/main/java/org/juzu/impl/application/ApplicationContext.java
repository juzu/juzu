package org.juzu.impl.application;

import org.juzu.RenderScoped;
import org.juzu.Resource;
import org.juzu.application.ApplicationDescriptor;
import org.juzu.application.Phase;
import org.juzu.impl.cdi.Export;
import org.juzu.impl.cdi.ScopeController;
import org.juzu.impl.request.ControllerParameter;
import org.juzu.impl.spi.cdi.Container;
import org.juzu.impl.request.ActionContext;
import org.juzu.impl.request.ControllerMethod;
import org.juzu.impl.request.RenderContext;
import org.juzu.impl.request.RequestContext;
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

   public static RequestContext getCurrentRequest()
   {
      return current.get();
   }

   /** . */
   private final ApplicationDescriptor descriptor;

   /** . */
   private final Container container;

   /** . */
   private final ControllerResolver controllerResolver;

   /** . */
   private static final ThreadLocal<RequestContext> current = new ThreadLocal<RequestContext>();

   public ApplicationContext()
   {
      Bootstrap bootstrap = Bootstrap.foo.get();

      //
      this.descriptor = bootstrap.descriptor;
      this.container = bootstrap.container;
      this.controllerResolver = new ControllerResolver(bootstrap.descriptor);
   }

   public ApplicationDescriptor getDescriptor()
   {
      return descriptor;
   }

   public void invoke(RequestContext context)
   {
      try
      {
         current.set(context);
         ScopeController.start(context.getPhase());

         //
         if (context instanceof RenderContext)
         {
            doInvoke(context);
         }
         else if (context instanceof ActionContext)
         {
            doInvoke(context);
         }
         else
         {
            throw new UnsupportedOperationException();
         }
      }
      finally
      {
         current.set(null);
         ScopeController.stop();
      }
   }


   private void doInvoke(RequestContext context)
   {
      ControllerMethod method = controllerResolver.resolve(context.getPhase(), context.getParameters());

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

               // Prepare method parameters
               List<ControllerParameter> params = method.getArgumentParameters();
               Object[] args = new Object[params.size()];
               for (int i = 0;i < args.length;i++)
               {
                  String[] values = context.getParameters().get(params.get(i).getName());
                  args[i] = values[0];
               }

               // For now we do only zero arg invocations
               method.getMethod().invoke(o, args);
            }
            catch (Exception e)
            {
               throw new UnsupportedOperationException("handle me gracefully", e);
            }
         }
      }
   }

   @Produces
   @RenderScoped
   public Printer getPrinter()
   {
      RequestContext context = current.get();
      if (context instanceof RenderContext)
      {
         return ((RenderContext)context).getPrinter();
      }
      else
      {
         throw new AssertionError("does not make sense");
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
