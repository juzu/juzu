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

import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class QN2TestCase extends AbstractTestCase {

  @Test
  public void testIAE() {
    assertIAE(".");
    assertIAE(".a");
    assertIAE("a.");
    assertIAE("a..b");
    assertIAE("ab..c");
  }

  private void assertIAE(String value) {
    try {
      QN.parse(value);
      fail("Was expecting " + value + " to fail");
    }
    catch (IllegalArgumentException ignore) {
    }
  }

  @Test
  public void testValues() {
    assertQN("a.b", "a", "b");
    assertQN("a.b.c", "a", "b", "c");
  }

  private void assertQN(String value, String... names) {
    QN qn = QN.parse(value);
    ArrayList<String> testNames = Tools.list(qn);
    assertEquals(Arrays.asList(names), testNames);
    assertEquals(value, qn.getValue());
  }

  @Test
  public void testEmpty() {
    QN empty = QN.parse("");
    assertEquals(0, empty.size());
    assertEquals(0, empty.length());
    assertEquals(Collections.emptyList(), Tools.list(empty));
    assertNull(empty.getParent());
    assertEquals("", empty.getValue());
  }

  @Test
  public void testSimple() {
    QN simple = QN.parse("a");
    assertEquals(1, simple.size());
    assertEquals(1, simple.length());
    assertEquals(Collections.singletonList("a"), Tools.list(simple));
    QN parent = simple.getParent();
    assertEquals(0, parent.size());
    assertEquals(0, parent.length());
    assertEquals("a", simple.getValue());
  }

  @Test
  public void testAppend() {
    assertAppend("a.b", "a", "b");
    assertAppend("a.b.c", "a.b", "c");
    try {
      QN.parse("a").append("");
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    try {
      QN.parse("a").append("a.b");
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    try {
      QN.parse("a").append((String)null);
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    try {
      QN.parse("a").append((String[])null);
      fail();
    }
    catch (NullPointerException e) {
    }
  }

  private void assertAppend(String expected, String qn, String simpleName) {
    QN parsed = QN.parse(qn);
    QN appended = parsed.append(simpleName);
    assertEquals(expected, appended.getValue());
    assertEquals(appended.size(), parsed.size() + 1);
  }

  @Test
  public void testPrefix() {
    assertPrefix("", "");
    assertPrefix("", "a");
    assertPrefix("", "a.b");
    assertPrefix("", "a.b.c");
    assertNotPrefix("a", "");
    assertPrefix("a", "a");
    assertPrefix("a", "a.b");
    assertPrefix("a", "a.b.c");
    assertNotPrefix("a.b", "");
    assertNotPrefix("a.b", "a");
    assertPrefix("a.b", "a.b");
    assertPrefix("a.b", "a.b.c");
    assertNotPrefix("a.b.c", "");
    assertNotPrefix("a.b.c", "a");
    assertNotPrefix("a.b.c", "a.b");
    assertPrefix("a.b.c", "a.b.c");
  }

  private void assertPrefix(String prefix, String s) {
    assertTrue(QN.parse(prefix).isPrefix(QN.parse(s)));
  }

  private void assertNotPrefix(String prefix, String s) {
    assertFalse(QN.parse(prefix).isPrefix(QN.parse(s)));
  }

  @Test
  public void testParent() {
    QN abc = QN.parse("a.b.c");
    QN ab = abc.getParent();
    assertNotNull(ab);
    assertEquals(QN.parse("a.b"), ab);
    QN a = ab.getParent();
    assertEquals(QN.parse("a"), a);
    QN empty = a.getParent();
    assertEquals(0, empty.size());
    assertEquals(0, empty.length());
  }
}
