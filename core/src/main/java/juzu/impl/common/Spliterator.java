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
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator that splits a string into chunks without requiring to allocate an array to hold the various chunks of the
 * splitted string.
 * <p/>
 * <ul> <li>"" -> ()</li> <li>"." -> ("","")</li> <li>"a" -> ("a")</li> </ul>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Spliterator implements Iterator<String> {

  public static Iterable<String> split(
      final String s,
      final char separator) throws NullPointerException {
    return new Iterable<String>() {
      public Iterator<String> iterator() {
        return new Spliterator(s, separator);
      }
    };
  }

  public static Iterable<String> split(
      final String s,
      final int from,
      final int to,
      final char separator) throws NullPointerException, IndexOutOfBoundsException {
    return new Iterable<String>() {
      public Iterator<String> iterator() {
        return new Spliterator(s, from, to, separator);
      }
    };
  }

  public static <C extends Collection<String>> C split(
      final String s,
      final char separator,
      C collection) throws NullPointerException {
    for (Spliterator i = new Spliterator(s, separator);i.hasNext();) {
      collection.add(i.next());
    }
    return collection;
  }

  public static <C extends Collection<String>> C split(
      final String s,
      final int from,
      final int to,
      final char separator, C collection) throws NullPointerException {
    for (Spliterator i = new Spliterator(s, from, to, separator);i.hasNext();) {
      collection.add(i.next());
    }
    return collection;
  }

  /** . */
  private final CharSequence s;

  /** . */
  private final char separator;

  /** . */
  private int from;

  /** . */
  private int current;

  /** . */
  private int to;

  /**
   * Creates a spliterator.
   *
   * @param s         the string to split
   * @param separator the separator
   * @throws NullPointerException if the string is null
   */
  public Spliterator(String s, char separator) throws NullPointerException {
    this(s, 0, s.length(), separator);
  }

  /**
   * Creates a spliterator.
   *
   * @param s         the string to split
   * @param from      the lower bound
   * @param to        the upper bound
   * @param separator the separator
   * @throws NullPointerException if the string is null
   * @throws IndexOutOfBoundsException if any bound is incorrect
   */
  public Spliterator(CharSequence s, int from, int to, char separator) throws NullPointerException, IndexOutOfBoundsException {
    if (s == null) {
      throw new NullPointerException();
    }
    if (from < 0) {
      throw new IndexOutOfBoundsException("Lower bound cannot be negative");
    }
    if (to > s.length()) {
      throw new IndexOutOfBoundsException("Upper bound cannot be greater than sequence length");
    }
    if (from > to) {
      throw new IndexOutOfBoundsException("Upper bound cannot be greater than lower bound");
    }

    //
    int pos = Tools.indexOf(s, separator, from);

    //
    this.s = s;
    this.separator = separator;
    this.from = from;
    this.current = pos == -1 ? to : pos;
    this.to = to;
  }

  public boolean hasNext() {
    return current != -1;
  }

  public String next() {
    if (hasNext()) {
      String next = s.subSequence(from, current).toString();
      if (current < to) {
        from = current + 1;
        int pos = Tools.indexOf(s, separator, current + 1);
        if (pos == -1) {
          current = to;
        } else {
          current = pos;
        }
      } else {
        current = -1;
      }
      return next;
    }
    else {
      throw new NoSuchElementException();
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}
