package org.juzu.impl.cdi;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

/**
 * Juzu CDI extension.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class JuzuExt implements Extension
{

   public JuzuExt()
   {
   }

   <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat)
   {
      AnnotatedType<T> annotatedType = pat.getAnnotatedType();
      Class<?> type = annotatedType.getJavaClass();
      if (type.getName().startsWith("org.juzu."))
      {
         boolean present = annotatedType.isAnnotationPresent(Export.class);
         if (!present)
         {
            pat.veto();
         }
      }
   }

   void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager)
   {
      event.addContext(ScopeController.INSTANCE.requestContext);
      event.addContext(ScopeController.INSTANCE.actionContext);
      event.addContext(ScopeController.INSTANCE.renderContext);
      event.addContext(ScopeController.INSTANCE.sessionContext);
   }
}
