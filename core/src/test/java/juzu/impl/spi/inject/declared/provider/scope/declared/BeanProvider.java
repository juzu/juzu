package juzu.impl.spi.inject.declared.provider.scope.declared;

import javax.inject.Provider;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BeanProvider implements Provider<Bean>
{

   /** . */
   public static Bean bean = new Bean();

   public Bean get()
   {
      return bean;
   }
}
