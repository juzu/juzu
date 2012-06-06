package juzu.impl.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class EmptyParameterMap implements ParameterMap
{

   /** . */
   private final Map<String, String[]> EMPTY = Collections.emptyMap();

   EmptyParameterMap()
   {
   }

   public int size()
   {
      return EMPTY.size();
   }

   public boolean isEmpty()
   {
      return EMPTY.isEmpty();
   }

   public boolean containsKey(Object key)
   {
      return EMPTY.containsKey(key);
   }

   public boolean containsValue(Object value)
   {
      return EMPTY.containsValue(value);
   }

   public String[] get(Object key)
   {
      return EMPTY.get(key);
   }

   public String[] put(String key, String[] value)
   {
      return EMPTY.put(key, value);
   }

   public String[] remove(Object key)
   {
      return EMPTY.remove(key);
   }

   public void putAll(Map<? extends String, ? extends String[]> m)
   {
      EMPTY.putAll(m);
   }

   public void clear()
   {
      EMPTY.clear();
   }

   public Set<String> keySet()
   {
      return EMPTY.keySet();
   }

   public Collection<String[]> values()
   {
      return EMPTY.values();
   }

   public Set<Entry<String, String[]>> entrySet()
   {
      return EMPTY.entrySet();
   }

   @Override
   public boolean equals(Object o)
   {
      return EMPTY.equals(o);
   }

   @Override
   public int hashCode()
   {
      return EMPTY.hashCode();
   }

   public void setParameter(String name, String value) throws NullPointerException
   {
      throw new UnsupportedOperationException("Immutable");
   }

   public void setParameter(String name, String[] value) throws NullPointerException, IllegalArgumentException
   {
      throw new UnsupportedOperationException("Immutable");
   }

   public void setParameters(Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException
   {
      throw new UnsupportedOperationException("Immutable");
   }
}
