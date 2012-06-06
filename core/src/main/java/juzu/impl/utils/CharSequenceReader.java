/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package juzu.impl.utils;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CharSequenceReader {

  /** . */
  private final CharSequence s;

  /** . */
  private char[] unread;

  /** . */
  private int pos;

  /** . */
  private int index;

  public CharSequenceReader(CharSequence s) {
    this.s = s;
    this.unread = null;
    this.pos = 0;
    this.index = 0;
  }

  public int read() {
    if (pos > 0) {
      return unread[--pos];
    }
    else {
      if (index < s.length()) {
        return s.charAt(index++);
      }
      else {
        return -1;
      }
    }
  }

  public void unread(int c) {
    if (unread == null) {
      unread = new char[10];
    }
    if (pos + 1 < unread.length) {
      unread[pos++] = (char)c;
    }
    else {
      throw new IllegalStateException("Buffer full");
    }
  }
}
