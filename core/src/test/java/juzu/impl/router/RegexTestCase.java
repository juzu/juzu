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

import juzu.impl.router.regex.JRegexFactory;
import juzu.impl.router.regex.RE;
import juzu.test.AbstractTestCase;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RegexTestCase extends AbstractTestCase {

  @Test
  public void testLiteral() {
    RE regex = JRegexFactory.INSTANCE.compile("abc");
    RE.Match[] matches = regex.matcher().find("abc");
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getStart());
    assertEquals(3, matches[0].getEnd());
    assertEquals("abc", matches[0].getValue());
  }

  @Test
  public void testSimpleGroup1() {
    RE regex = JRegexFactory.INSTANCE.compile("(abc)");
    RE.Match[] matches = regex.matcher().find("abc");
    assertEquals(2, matches.length);
    assertEquals(0, matches[0].getStart());
    assertEquals(3, matches[0].getEnd());
    assertEquals("abc", matches[0].getValue());
    assertEquals(0, matches[1].getStart());
    assertEquals(3, matches[1].getEnd());
    assertEquals("abc", matches[1].getValue());
  }

  @Test
  public void testSimpleGroup2() {
    RE regex = JRegexFactory.INSTANCE.compile("a(b)c");
    RE.Match[] matches = regex.matcher().find("abc");
    assertEquals(2, matches.length);
    assertEquals(0, matches[0].getStart());
    assertEquals(3, matches[0].getEnd());
    assertEquals("abc", matches[0].getValue());
    assertEquals(1, matches[1].getStart());
    assertEquals(2, matches[1].getEnd());
    assertEquals("b", matches[1].getValue());
  }

  @Test
  public void testNonCapturingGroup() {
    RE regex = JRegexFactory.INSTANCE.compile("a(?:b)c");
    RE.Match[] matches = regex.matcher().find("abc");
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getStart());
    assertEquals(3, matches[0].getEnd());
    assertEquals("abc", matches[0].getValue());
  }
}
