/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
