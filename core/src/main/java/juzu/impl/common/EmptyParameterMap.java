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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class EmptyParameterMap implements ParameterMap {

  /** . */
  private final Map<String, String[]> EMPTY = Collections.emptyMap();

  EmptyParameterMap() {
  }

  public int size() {
    return EMPTY.size();
  }

  public boolean isEmpty() {
    return EMPTY.isEmpty();
  }

  public boolean containsKey(Object key) {
    return EMPTY.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return EMPTY.containsValue(value);
  }

  public String[] get(Object key) {
    return EMPTY.get(key);
  }

  public String[] put(String key, String[] value) {
    return EMPTY.put(key, value);
  }

  public String[] remove(Object key) {
    return EMPTY.remove(key);
  }

  public void putAll(Map<? extends String, ? extends String[]> m) {
    EMPTY.putAll(m);
  }

  public void clear() {
    EMPTY.clear();
  }

  public Set<String> keySet() {
    return EMPTY.keySet();
  }

  public Collection<String[]> values() {
    return EMPTY.values();
  }

  public Set<Entry<String, String[]>> entrySet() {
    return EMPTY.entrySet();
  }

  @Override
  public boolean equals(Object o) {
    return EMPTY.equals(o);
  }

  @Override
  public int hashCode() {
    return EMPTY.hashCode();
  }

  public void setParameter(String name, String value) throws NullPointerException {
    throw new UnsupportedOperationException("Immutable");
  }

  public void setParameter(String name, String[] value) throws NullPointerException, IllegalArgumentException {
    throw new UnsupportedOperationException("Immutable");
  }

  public void setParameters(Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException {
    throw new UnsupportedOperationException("Immutable");
  }
}
