package org.juzu.impl.application;

import org.juzu.application.ApplicationDescriptor;
import org.juzu.impl.spi.cdi.Container;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Bootstrap
{

   /** . */
   static final ThreadLocal<Bootstrap> foo = new ThreadLocal<Bootstrap>();

   /** . */
   final Container container;

   /** . */
   final ApplicationDescriptor descriptor;

   /** . */
   private ApplicationContext context;

   public Bootstrap(Container container, ApplicationDescriptor descriptor)
   {
      this.container = container;
      this.descriptor = descriptor;
   }

   public void start() throws Exception
   {
      foo.set(this);
      try
      {
         container.start();

         // Make the bean available and force bean creation so it get
         // the thread local in this context
         BeanManager mgr = container.getManager();
         Bean bean = mgr.getBeans(ApplicationContext.class).iterator().next();
         CreationalContext<?> cc = mgr.createCreationalContext(bean);
         this.context = (ApplicationContext)mgr.getReference(bean, ApplicationContext.class, cc);
      }
      finally
      {
         foo.set(null);
      }
   }

   public ApplicationContext getContext()
   {
      return context;
   }

   public void stop()
   {
      container.stop();
   }
}
