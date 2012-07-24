/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.impl.common;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple JSON object useful for marshalling and unmarshalling data.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class JSON implements Serializable {

  /** . */
  private static final String[] EMPTY_STRING_ARRAY = new String[0];

  /** . */
  private static final ScriptEngine engine;

  static {
    try {
      String init = Tools.read(Tools.class.getResource("json.js"));
      ScriptEngine tmp = new ScriptEngineManager().getEngineByName("JavaScript");
      tmp.put("JSON", new JSON());
      tmp.eval(init);
      engine = tmp;
    }
    catch (Exception e) {
      // Unexpected
      throw new AssertionError(e);
    }
  }

  private static final Set<Class<?>> simpleTypes = new HashSet<Class<?>>(Arrays.asList(
    Integer.class,
    Long.class,
    Boolean.class
  ));

  public static JSON json() {
    return new JSON();
  }

  /** . */
  private final TreeMap<String, Object> entries = new TreeMap<String, Object>();

  public Set<String> names() {
    return entries.keySet();
  }

  public Object get(String name) {
    return entries.get(name);
  }

  public String getString(String name) {
    return (String)entries.get(name);
  }

  public List<?> getList(String name) {
    return (List<?>)entries.get(name);
  }

  public <E> List<? extends E> getList(String name, Class<E> elementType) {
    List<?> entry = (List<?>)entries.get(name);
    int len = entry != null ? entry.size() : 0;
    for (int i = 0;i < len;i++) {
      Object obj = entry.get(i);
      if (!elementType.isInstance(obj)) {
        throw new ClassCastException("Cannot cast " + obj + "with class " + obj.getClass().getName() + " to class " + elementType.getName());
      }
    }
    return (List<E>)entry;
  }

  public <E> E[] getArray(String name, Class<E> elementType) {
    List<?> entry = (List<?>)entries.get(name);
    if (entry == null) {
      return (E[])Array.newInstance(elementType, 0);
    }
    else {
      int len = entry.size();
      Object array = Array.newInstance(elementType, len);
      for (int i = 0;i < len;i++) {
        Object obj = entry.get(i);
        if (!elementType.isInstance(obj)) {
          throw new ClassCastException("Cannot cast " + obj + "with class " + obj.getClass().getName() + " to class " + elementType.getName());
        }
        Array.set(array, i, obj);
      }
      return (E[])array;
    }
  }

  public Boolean getBoolean(String name) {
    return (Boolean)entries.get(name);
  }

  public JSON getJSON(String name) {
    return (JSON)entries.get(name);
  }

  public boolean contains(String name) {
    return entries.containsKey(name);
  }

  public JSON clear() {
    entries.clear();
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof JSON) {
      JSON that = (JSON)obj;
      return entries.equals(that.entries);
    }
    return false;
  }

  public <E> JSON merge(JSON json) {
    return merge(json.entries);
  }

  public <E> JSON merge(Map<String, E> map) {
    for (Map.Entry<String, E> entry : map.entrySet()) {
      set(entry.getKey(), entry.getValue());
    }
    return this;
  }

  public <E> JSON map(String name, Map<String, E> map) {
    return set(name, map);
  }

  public JSON list(String name) {
    return set(name, EMPTY_STRING_ARRAY);
  }

  public <E> JSON list(String name, E... elements) {
    return set(name, elements);
  }

  public <E> JSON map(String name, Iterable<E> elements) {
    return set(name, elements);
  }

  public JSON set(String name, Object o) {
    entries.put(name, unwrap(o));
    return this;
  }

  public TreeMap<String, Object> build() {
    TreeMap<String, Object> ret = new TreeMap<String, Object>(entries);
    for (java.util.Map.Entry<String, Object> entry : ret.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof JSON) {
        JSON json = (JSON)value;
        entry.setValue(json.build());
      }
    }
    return ret;
  }

  private static Object unwrap(Object o) {
    if (o == null || o instanceof JSON) {
      // Ok
    }
    else {
      Class<?> type = o.getClass();

      //
      if (simpleTypes.contains(type)) {
        // Ok
      }
      else {
        if (o instanceof Map<?, ?>) {
          Map<?, ?> map = (Map<?, ?>)o;
          JSON json = new JSON();
          for (Map.Entry<?, ?> entry : map.entrySet()) {
            String name = entry.getKey().toString();
            Object value = unwrap(entry.getValue());
            json.entries.put(name, value);
          }
          o = json;
        }
        else if (o instanceof Iterable<?>) {
          ArrayList<Object> r = new ArrayList<Object>();
          for (Object element : (Iterable<?>)o) {
            Object unwrapped = unwrap(element);
            r.add(unwrapped);
          }
          o = r;
        }
        else if (o.getClass().isArray()) {
          int length = Array.getLength(o);
          ArrayList<Object> r = new ArrayList<Object>(length);
          for (int i = 0;i < length;i++) {
            Object component = Array.get(o, i);
            Object unwrapped = unwrap(component);
            r.add(unwrapped);
          }
          o = r;
        }
        else {
          Object json = null;
          try {
            Method toJSON = type.getMethod("toJSON");
            if (toJSON.getReturnType() != Void.TYPE) {
              try {
                json = toJSON.invoke(o);
              }
              catch (IllegalAccessException e) {
                throw new AssertionError(e);
              }
              catch (InvocationTargetException e) {
                Throwable t = e.getCause();
                if (t instanceof RuntimeException) {
                  throw (RuntimeException)t;
                }
                else if (t instanceof Error) {
                  throw (Error)t;
                }
                else {
                  throw new UndeclaredThrowableException(t);
                }
              }
            }
          }
          catch (NoSuchMethodException ignore) {
          }

          //
          if (json != null) {
            o = unwrap(json);
          }
          else {
            o = o.toString();
          }
        }
      }
    }

    //
    return o;
  }

  public <A extends Appendable> A toString(A appendable) throws IOException {
    return toString(this, appendable);
  }

  public <A extends Appendable> A toString(A appendable, int indent) throws IOException {
    return toString(this, appendable, indent);
  }

  public String toString() {
    try {
      return toString(new StringBuilder()).toString();
    }
    catch (IOException e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  public static <A extends Appendable> A toString(Object o, A appendable) throws IOException {
    return toString(o, appendable, 0);
  }

  public static <A extends Appendable> A toString(Object o, A appendable, int indent) throws IOException {
    return toString(o, appendable, 0, indent);
  }

  private static <A extends Appendable> A toString(Object o, A appendable, final int margin, final int indent) throws IOException {
    if (o == null) {
      appendable.append("null");
    }
    else if (o instanceof JSON) {
      JSON m = (JSON)o;
      appendable.append('{');
      for (Iterator<? extends Map.Entry<?, ?>> iterator = m.entries.entrySet().iterator();iterator.hasNext();) {
        Map.Entry<?, ?> entry = iterator.next();
        if (indent > 0) {
          appendable.append('\n');
          pad(appendable, margin + indent);
        }
        appendable.append('"');
        appendable.append(entry.getKey().toString());
        appendable.append("\":");
        toString(entry.getValue(), appendable, margin + indent, indent);
        if (iterator.hasNext()) {
          appendable.append(',');
        }
        else if (indent > 0) {
          appendable.append('\n');
          pad(appendable, margin);
        }
      }
      appendable.append('}');
    }
    else if (o instanceof List) {
      appendable.append('[');
      List<?> list = (List<?>)o;
      for (Iterator<?> i = list.iterator();i.hasNext();) {
        Object e = i.next();
        toString(e, appendable, margin + indent, indent);
        if (i.hasNext()) {
          appendable.append(',');
        }
      }
      appendable.append("]");
    }
    else if (o instanceof Boolean || o instanceof Number) {
      appendable.append(o.toString());
    }
    else {
      appendable.append('"');
      CharSequence s = o instanceof CharSequence ? (CharSequence)o : o.toString();
      for (int i = 0, len = s.length();i < len;i++) {
        char c = s.charAt(i);
        switch (c) {
          case '"':
            appendable.append("\\\"");
            break;
          case '\n':
            appendable.append("\\n");
            break;
          case '\r':
            appendable.append("\\r");
            break;
          case '\b':
            appendable.append("\\b");
            break;
          case '\f':
            appendable.append("\\f");
            break;
          case '\t':
            appendable.append("\\t");
            break;
          default:
            appendable.append(c);
        }
      }
      appendable.append('"');
    }

    //
    return appendable;
  }

  private static void pad(Appendable appendable, int size) throws IOException {
    while (size-- > 0) {
      appendable.append(' ');
    }
  }

  public static Object parse(String json) {
    try {
      Bindings bindings = new SimpleBindings();
      String eval = "var tmp = (" + json + ");var o = new java.util.concurrent.atomic.AtomicReference(tmp.toJava());";
      engine.eval(eval, bindings);
      AtomicReference ret = (AtomicReference)bindings.get("o");
      return ret.get();
    }
    catch (ScriptException e) {
      throw new AssertionError(e);
    }
  }
}
