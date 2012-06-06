package juzu.impl.spi.inject.spring;

import juzu.impl.inject.Scoped;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class SpringScoped implements Scoped
{

   /** . */
   final DefaultListableBeanFactory factory;

   /** . */
   final String bean;
   
   /** . */
   Object o;

   /** . */
   Runnable destructionCallback;

   SpringScoped(DefaultListableBeanFactory factory, String bean)
   {
      this.factory = factory;
      this.bean = bean;
      this.o = null;
      this.destructionCallback = null;
   }

   public Object get()
   {
      return o;
   }

   public void destroy()
   {
      if (destructionCallback != null)
      {
         destructionCallback.run();
      }
   }
}
