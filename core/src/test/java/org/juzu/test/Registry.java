package org.juzu.test;

import java.util.HashMap;
import java.util.Map;

/**
 * A static map used in various manner by tests.
 *
 *  @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Registry
{

   private static final Map<Object, Object> state = new HashMap<Object, Object>();

   public static <T> T get(Object key)
   {
      if (key == null)
      {
         throw new NullPointerException();
      }
      // Absolutely not type safe, but we don't care, it's for testing
      @SuppressWarnings("unchecked")
      T t = (T)state.get(key);
      return t;
   }

   public static <T> void set(Object key, T value)
   {
      if (key == null)
      {
         throw new NullPointerException();
      }
      if (value != null)
      {
         state.put(key, value);
      }
      else
      {
         state.remove(key);
      }
   }

   public static <T> T unset(Object key)
   {
      if (key == null)
      {
         throw new NullPointerException();
      }
      return (T)state.remove(key);
   }

   public void clear()
   {
      state.clear();
   }
}
