package org.juzu.impl.spi.inject.spring;

import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class SingletonBean extends AbstractBean
{

   /** . */
   final Object instance;

   SingletonBean(Object instance, Iterable<Annotation> qualifiers)
   {
      super(instance.getClass(), qualifiers);

      //
      this.instance = instance;
   }
}
