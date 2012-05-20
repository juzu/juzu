package org.juzu.plugin.less.impl.lesser;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Compilation extends Result
{

   /** . */
   private final String value;

   public Compilation(String value)
   {
      this.value = value;
   }

   public String getValue()
   {
      return value;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder("Compilation[");
      if (value.length() < 40)
      {
         sb.append(value);
      }
      else
      {
         sb.append(value, 0, 40).append("...");
      }
      sb.append("]");
      return sb.toString();
   }
}
