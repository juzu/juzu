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

package juzu.impl.router;

/**
 * A bit stack optimized for speed.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class BitStack<T> {

  /** . */
  private static final Object[] EMPTY = new Object[0];

  private static class Frame {

    /** . */
    private Frame previous;

    /** . */
    private Frame next;

    /** . */
    private Object[] bits;

    /** . */
    private int cardinality;

    private Frame(int size) {
      this(new Object[size], 0);
    }

    private Frame(Object[] bits, int cardinality) {
      this.previous = null;
      this.next = null;
      this.bits = bits;
      this.cardinality = cardinality;
    }
  }

  /** . */
  private Frame head;

  /** . */
  private Frame current;

  /** . */
  private int depth;

  /** . */
  private int size;

  BitStack() {
    this.current = null;
    this.depth = 0;
    this.size = 0;
  }

  void init(int size) {
    if (depth > 0) {
      throw new IllegalStateException();
    }
    if (size < 0) {
      throw new IllegalArgumentException();
    }
    this.size = size;
  }

  void reset() {
    if (depth > 0) {
      throw new IllegalStateException();
    }
    this.size = 0;
  }

  void push() {
    if (current == null) {
      if (head == null) {
        head = new Frame(size);
      }
      current = head;
      depth++;
    }
    else {
      if (current.next == null) {
        Frame next = new Frame(current.bits.clone(), current.cardinality);
        current.next = next;
        next.previous = current;
        current = next;
        depth++;
      }
      else {
        Frame next = current.next;
        System.arraycopy(current.bits, 0, next.bits, 0, current.bits.length);
        next.cardinality = current.cardinality;
        current = next;
        depth++;
      }
    }
  }

  void pop() {
    if (depth == 0) {
      throw new IllegalStateException();
    }
    current = current.previous;
    depth--;
  }

  int getDepth() {
    return depth;
  }

  void set(int index, T value) {
    if (index > 63) {
      throw new IllegalArgumentException("Index " + index + "  > 63 not allowed");
    }
    if (value == null) {
      throw new NullPointerException();
    }
    if (depth < 1) {
      throw new IllegalStateException();
    }
    if (index > size) {
      throw new IllegalArgumentException();
    }
    Object pre = current.bits[index];
    if (pre != null) {
      throw new IllegalStateException();
    }
    current.bits[index] = value;
    current.cardinality++;
  }

  T get(int index) {
    if (index > 63) {
      throw new IllegalArgumentException("Index " + index + "  > 63 not allowed");
    }
    if (depth < 1) {
      throw new IllegalStateException();
    }
    if (index > size) {
      throw new IllegalArgumentException();
    }
    return (T)current.bits[index];
  }

  boolean isEmpty() {
    if (depth < 1) {
      throw new IllegalStateException();
    }
    return current.cardinality == size;
  }
}
