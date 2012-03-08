package org.juzu.impl.spi.inject.spring;

import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class DeclaredBean extends AbstractBean
{

   DeclaredBean(Class<?> type, Iterable<Annotation> qualifiers)
   {
      super(type, qualifiers);
   }
}
