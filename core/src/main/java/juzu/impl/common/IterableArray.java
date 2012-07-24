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

import java.util.Iterator;
import java.util.NoSuchElementException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class IterableArray<E> implements Iterable<E> {

  /** . */
  private final E[] elements;

  /** . */
  private final int from;

  /** . */
  private final int to;

  public IterableArray(E[] elements, int from, int to) throws NullPointerException, IndexOutOfBoundsException, IllegalArgumentException {
    if (elements == null) {
      throw new NullPointerException("No null elements accepted");
    }
    if (from < 0) {
      throw new IndexOutOfBoundsException("From index cannot be negative");
    }
    if (to > elements.length + 1) {
      throw new IndexOutOfBoundsException("To index cannot be greater than the array size + 1");
    }
    if (from > to) {
      throw new IllegalArgumentException("From index cannot be greater than the to index");
    }

    //
    this.elements = elements;
    this.from = from;
    this.to = to;
  }

  public Iterator<E> iterator() {
    return new Iterator<E>() {
      int current = from;

      public boolean hasNext() {
        return current < to;
      }

      public E next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return elements[current++];
      }

      public void remove() {
        throw new NoSuchElementException();
      }
    };
  }
}
