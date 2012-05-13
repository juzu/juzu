package org.juzu.asset;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public enum AssetLocation
{

   /**
    * An external script.
    */
   EXTERNAL,

   /**
    * A server served script.
    */
   SERVER,

   /**
    * A classpath served script.
    */
   CLASSPATH;

   public static AssetLocation safeValueOf(String name)
   {
      if (name != null)
      {
         try
         {
            return valueOf(name);
         }
         catch (IllegalArgumentException e)
         {
            // Should log as warning ?
         }
      }
      return AssetLocation.SERVER;
   }
}
