package org.juzu.impl.request;

import org.juzu.impl.utils.Safe;

/**
 * A parameter of a controller
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ControllerParameter
{

   /** . */
   private final String name;

   /** . */
   private final String value;

   public ControllerParameter(String name) throws NullPointerException
   {
      this(name, null);
   }

   public ControllerParameter(String name, String value) throws NullPointerException
   {
      if (name == null)
      {
         throw new NullPointerException("No null parameter name accepted");
      }

      //
      this.name = name;
      this.value = value;
   }

   /**
    * Returns the parameter name.
    *
    * @return the parameter name
    */
   public String getName()
   {
      return name;
   }

   /**
    * Returns the value matched by a controller parameter or null if the parameter can match any value.
    *
    * @return the parameter value
    */
   public String getValue()
   {
      return value;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      else if (obj instanceof ControllerParameter)
      {
         ControllerParameter that = (ControllerParameter)obj;
         return name.equals(that.name) && Safe.equals(value, that.value);
      }
      else
      {
         return false;
      }
   }

   @Override
   public String toString()
   {
      return "ControllerParameter[name=" + name + ",value=" + value + "]";
   }
}
