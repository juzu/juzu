package org.juzu;

import java.lang.reflect.ParameterizedType;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class PropertyType<T>
{

   /** . */
   private final Class<T> type;

   protected PropertyType() throws NullPointerException
   {
      ParameterizedType pt = (ParameterizedType)getClass().getGenericSuperclass();
      this.type = (Class)pt.getActualTypeArguments()[0];
   }

   public Class<T> getType()
   {
      return type;
   }

   @Override
   public boolean equals(Object obj)
   {
      return obj == this || obj != null && getClass().equals(obj.getClass());
   }

   @Override
   public int hashCode()
   {
      return getClass().hashCode();
   }
}
