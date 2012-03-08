package org.juzu.impl.spi.inject.cdi;

import javax.enterprise.context.spi.CreationalContext;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class AbstractSingletonBean extends AbstractBean
{

   protected AbstractSingletonBean(Class<?> type, Iterable<Annotation> qualifiers)
   {
      super(type, qualifiers);
   }

   public Class<? extends Annotation> getScope()
   {
      return Singleton.class;
   }

   public void destroy(Object instance, CreationalContext ctx)
   {
      ctx.release();
   }
}
