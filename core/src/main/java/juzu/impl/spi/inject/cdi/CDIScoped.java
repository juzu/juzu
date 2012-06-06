package juzu.impl.spi.inject.cdi;

import juzu.impl.inject.Scoped;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class CDIScoped<T> implements Scoped
{

   /** . */
   final Contextual<T> contextual;
   
   /** . */
   final CreationalContext<T> creationalContext;

   /** . */
   final T object;

   CDIScoped(Contextual<T> contextual, CreationalContext<T> creationalContext, T object)
   {
      this.contextual = contextual;
      this.creationalContext = creationalContext;
      this.object = object;
   }

   public Object get()
   {
      return object;
   }

   public void destroy()
   {
      contextual.destroy(object, creationalContext);
   }
}
