package org.juzu.impl.model.meta;

import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class Reference<T extends Serializable> implements Serializable
{

   /** . */
   final T target;

   /** . */
   final boolean strong;

   Reference(T target, boolean strong)
   {
      this.target = target;
      this.strong = strong;
   }

   @Override
   public String toString()
   {
      return "Reference[target=" + target + ",strong=" + strong + "]";
   }
}
