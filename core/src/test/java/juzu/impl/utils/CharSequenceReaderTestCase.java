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

import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CharSequenceReaderTestCase extends AbstractTestCase {

  @Test

  public void testEmpty() throws IOException {
    CharSequenceReader reader = new CharSequenceReader("");
    assertEquals(-1, reader.read());
  }

  @Test
  public void testSimple() throws IOException {
    CharSequenceReader reader = new CharSequenceReader("foo");
    assertEquals('f', reader.read());
    assertEquals('o', reader.read());
    assertEquals('o', reader.read());
    assertEquals(-1, reader.read());
  }

  @Test
  public void testUnread() throws IOException {
    CharSequenceReader reader = new CharSequenceReader("");
    reader.unread('o');
    reader.unread('o');
    reader.unread('f');
    assertEquals('f', reader.read());
    assertEquals('o', reader.read());
    assertEquals('o', reader.read());
    assertEquals(-1, reader.read());
  }
}
