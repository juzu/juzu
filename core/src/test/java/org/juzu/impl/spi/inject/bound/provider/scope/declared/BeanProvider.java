package org.juzu.impl.spi.inject.bound.provider.scope.declared;

import javax.inject.Provider;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BeanProvider implements Provider<Bean>
{

   /** . */
   public Bean bean = new Bean();

   public Bean get()
   {
      return bean;
   }
}
