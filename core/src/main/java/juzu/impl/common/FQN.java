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

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class FQN implements Serializable, Iterable<String> {

  /** . */
  private final String name;

  /** . */
  private final QN packageName;

  /** . */
  private final String simpleName;

  public FQN(Class<?> type) {
    this(type.getName());
  }

  public FQN(String name) {
    QN packageName;
    String simpleName;
    int pos = name.lastIndexOf('.');
    if (pos == -1) {
      packageName = QN.EMPTY;
      simpleName = name;
    }
    else {
      packageName = QN.parse(name.substring(0, pos));
      simpleName = name.substring(pos + 1);
    }

    //
    this.name = name;
    this.packageName = packageName;
    this.simpleName = simpleName;
  }

  public FQN(CharSequence packageName, CharSequence simpleName) {
    this(packageName.toString(), simpleName.toString());
  }

  public FQN(String packageName, String simpleName) {
    this(QN.parse(packageName), simpleName);
  }

  public FQN(QN packageName, String simpleName) {
    this.packageName = packageName;
    this.simpleName = simpleName;
    this.name = packageName.isEmpty() ? simpleName : packageName + "." + simpleName;
  }

  public String getName() {
    return name;
  }

  public QN getPackageName() {
    return packageName;
  }

  public String getSimpleName() {
    return simpleName;
  }

  public Iterator<String> iterator() {
    return new Iterator<String>() {

      /** . */
      private int index = 0;

      public boolean hasNext() {
        return index < packageName.size() + 1;
      }

      public String next() {
        if (index < packageName.size()) {
          return packageName.get(index++);
        }
        else if (index == packageName.size()) {
          index++;
          return simpleName;
        }
        else {
          throw new NoSuchElementException();
        }
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public int size() {
    return packageName.size() + 1;
  }

  public String get(int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("Index " + index + " cannot be negative");
    }
    else if (index < packageName.size()) {
      return packageName.get(index);
    }
    else if (index == packageName.size()) {
      return simpleName;
    }
    else {
      throw new IndexOutOfBoundsException("Index " + index + " cannot be greater than bound " + packageName.size() + 1);
    }
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof FQN) {
      FQN that = (FQN)obj;
      return packageName.equals(that.packageName) && simpleName.equals(that.simpleName);
    }
    return false;
  }

  @Override
  public String toString() {
    return name;
  }
}
