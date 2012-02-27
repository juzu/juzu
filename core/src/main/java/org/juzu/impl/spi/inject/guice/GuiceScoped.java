package org.juzu.impl.spi.inject.guice;

import org.juzu.impl.inject.Scoped;

import javax.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class GuiceScoped implements Scoped
{

   /** . */
   final Object o;

   GuiceScoped(Object o)
   {
      this.o = o;
   }

   public Object get()
   {
      return o;
   }

   public void destroy()
   {
      GuiceManager.invokePreDestroy(o);
   }
}
