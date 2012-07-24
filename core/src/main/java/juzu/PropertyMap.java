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

package juzu;

import juzu.impl.common.Tools;

import java.util.HashMap;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PropertyMap implements Iterable<PropertyType<?>> {

  /** . */
  private static final Object[] NO_VALUES = new Object[0];

  private static class Values implements Iterable<Object> {

    /** . */
    private Object[] objects = NO_VALUES;

    /** . */
    private int size = 0;

    /** . */
    private final PropertyMap owner;

    private Values(PropertyMap owner) {
      this.owner = owner;
    }

    private Values(PropertyMap owner, Values that) {
      this.owner = owner;
      this.objects = that.objects.clone();
      this.size = that.size;
    }

    public Iterator<Object> iterator() {
      return Tools.iterator(0, size, objects);
    }

    void addValue(Object value) {
      if (size >= objects.length) {
        Object[] copy = new Object[size + 4];
        System.arraycopy(objects, 0, copy, 0, objects.length);
        objects = copy;
      }
      objects[size++] = value;
    }

    void clear() {
      if (size > 0) {
        for (int i = size - 1;i >= 0;i--) {
          objects[i] = null;
        }
        size = 0;
      }
    }
  }

  /** The current existing values. */
  private HashMap<PropertyType<?>, Values> map;

  /** An optional delegate. */
  private PropertyMap delegate;

  public PropertyMap() {
  }

  public PropertyMap(PropertyMap delegate) {
    this.delegate = delegate;
  }

  private <T> Values get(PropertyType<T> property, boolean recurse, boolean modifiable) {
    Values values = null;

    //
    if (map != null) {
      values = map.get(property);
    }

    //
    if (values == null && recurse && delegate != null) {
      values = delegate.get(property, true, false);
    }

    //
    if (modifiable) {
      if (values == null) {
        if (map == null) {
          map = new HashMap<PropertyType<?>, Values>();
        }
        map.put(property, values = new Values(this));
      }
      else if (values.owner != this) {
        if (map == null) {
          map = new HashMap<PropertyType<?>, Values>();
        }
        map.put(property, values = new Values(this, values));
      }
    }
    else if (values != null && values.size == 0) {
      values = null;
    }

    //
    return values;
  }

  public Iterator<PropertyType<?>> iterator() {
    return map != null ? map.keySet().iterator() : Tools.<PropertyType<?>>emptyIterator();
  }

  public <T> T getValue(PropertyType<T> property) {
    T value = null;
    Values values = get(property, true, false);
    if (values != null && values.size > 0) {
      value = property.cast(values.objects[0]);
    }
    return value;
  }

  public <T> Iterable<T> getValues(PropertyType<T> property) throws NullPointerException {
    return (Iterable<T>)get(property, true, false);
  }

  public <T> void setValue(PropertyType<T> property, T value) throws NullPointerException {
    if (value == null) {
      remove(property);
    }
    else {
      Values existing = get(property, false, true);
      existing.clear();
      existing.addValue(value);
    }
  }

  public <T> void setValues(PropertyType<T> property, T... values) throws NullPointerException {
    Values existing = get(property, false, true);
    existing.clear();
    for (T value : values) {
      existing.addValue(value);
    }
  }

  public <T> void setValues(PropertyType<T> property, Iterable<? extends T> values) throws NullPointerException {
    Values existing = get(property, false, true);
    existing.clear();
    for (T value : values) {
      existing.addValue(value);
    }
  }

  public <T> void addValue(PropertyType<T> property, T value) throws NullPointerException {
    get(property, true, true).addValue(value);
  }

  public <T> void addValues(PropertyType<T> property, T... values) throws NullPointerException {
    Values existing = get(property, true, true);
    for (T value : values) {
      existing.addValue(value);
    }
  }

  public <T> void addValues(PropertyType<T> property, Iterable<? extends T> values) throws NullPointerException {
    Values existing = get(property, true, true);
    for (T value : values) {
      existing.addValue(value);
    }
  }

  public <T> void remove(PropertyType<T> property) throws NullPointerException {
    Values values = get(property, false, delegate != null);
    if (values != null) {
      values.clear();
    }
  }

  public <T> boolean contains(PropertyType<T> property) throws NullPointerException {
    Values values = get(property, true, false);
    return values != null && values.size > 0;
  }
}
