package org.juzu.impl.utils;

import java.util.HashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Builder
{

   public static <K, V> Map<K, V> map(K key, V value)
   {
      return new Map<K, V>().put(key, value);
   }

   public static class Map<K, V>
   {

      /** . */
      private java.util.Map<K, V> map;

      public Map()
      {
         this.map = new HashMap<K, V>();
      }

      public Map<K, V> put(K key, V value)
      {
         map.put(key, value);
         return this;
      }

      public java.util.Map<K, V> build()
      {
         return map;
      }
   }
}
