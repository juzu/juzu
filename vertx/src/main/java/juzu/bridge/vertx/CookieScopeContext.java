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
package juzu.bridge.vertx;

import com.google.inject.Key;
import juzu.impl.bridge.spi.ScopedContext;
import juzu.impl.common.Tools;
import juzu.impl.inject.Scoped;
import juzu.impl.inject.spi.guice.GuiceScoped;
import juzu.io.UndeclaredIOException;

import javax.inject.Named;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** @author Julien Viet */
public class CookieScopeContext implements ScopedContext {

  /** . */
  public static final int FLASH = 0;

  /** . */
  public static final int SESSION = 1;

  /** The current value. */
  HashMap<String, Scoped> values;

  /** The object from the request. */
  HashMap<String, String> snapshot;

  /** . */
  boolean purged;

  public Scoped get(Object key) throws NullPointerException {
    String name = nameOf(key);
    if (name != null) {
      Scoped scoped = null;
      if (values != null) {
        scoped = values.get(name);
      }
      if (scoped == null && snapshot != null) {
        String encoded = snapshot.get(name);
        if (encoded != null) {
          try {
            byte[] bytes = DatatypeConverter.parseBase64Binary(encoded);
            Object o = Tools.unserialize(Thread.currentThread().getContextClassLoader(), Serializable.class, new ByteArrayInputStream(bytes));
            if (values == null) {
              values = new HashMap<String, Scoped>();
            }
            values.put(name, scoped = new GuiceScoped(o));
          }
          catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      return scoped;
    }
    return null;
  }

  public void set(Object key, Scoped scoped) throws NullPointerException {
    String name = nameOf(key);
    if (name == null) {
      throw new UndeclaredIOException(new NotSerializableException("Type should be named with @" + Named.class.getSimpleName()));
    }
    if (!(scoped.get() instanceof Serializable)) {
      throw new UndeclaredIOException(new NotSerializableException("Could not serialize object"));
    }
    if (values == null) {
      values = new HashMap<String, Scoped>();
    }
    if (snapshot == null) {
      snapshot = new HashMap<String, String>();
    }
    values.put(name, scoped);
    if (!snapshot.containsKey(name)) {
      // We encode if so we can compare it with the actual value when sending cookies
      snapshot.put(name, encode((Serializable)scoped.get()));
    }
  }

  String encode(Serializable o) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Tools.serialize(o, baos);
      baos.close();
      return DatatypeConverter.printBase64Binary(baos.toByteArray());
    }
    catch (Exception e) {
      throw new RuntimeException("Could not serialize object " + o);
    }
  }

  private String nameOf(Object key) {
    Key gkey = (Key)key;
    Class<?> type = gkey.getTypeLiteral().getRawType();
    Named name = type.getAnnotation(Named.class);
    return name != null ? name.value() : null;
  }

  public int size() {
    return getNames().size();
  }

  Set<String> getNames() {
    HashSet<String> names = new HashSet<String>();
    if (values != null) {
      names.addAll(values.keySet());
    }
    if (snapshot != null) {
      names.addAll(snapshot.keySet());
    }
    return names;
  }

  public void close() {
    purged = true;
  }

  public Iterator<Scoped> iterator() {
    return Tools.emptyIterator();
  }
}
