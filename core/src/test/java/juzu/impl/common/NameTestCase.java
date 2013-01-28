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
public class NameTestCase extends AbstractTestCase {


  @Test
  public void testKind() {
    Name empty = Name.parse("");
    assertEquals("", empty.toString());
    assertTrue(empty.isEmpty());
    assertFalse(empty.isSimple());
    assertFalse(empty.isQualified());
    Name single = Name.parse("foo");
    assertEquals("foo", single.toString());
    assertFalse(single.isEmpty());
    assertTrue(single.isSimple());
    assertFalse(single.isQualified());
    Name qualified = Name.parse("foo.bar");
    assertEquals("foo.bar", qualified.toString());
    assertFalse(qualified.isEmpty());
    assertFalse(qualified.isSimple());
    assertTrue(qualified.isQualified());
  }

  @Test
  public void testParent() {
    Name abc = Name.parse("a.b.c");
    Name ab = abc.getParent();
    assertEquals(Name.parse("a.b"), ab);
    Name a = ab.getParent();
    assertEquals(a, ab.getParent());
    assertEquals(Name.parse("a"), a);
    Name empty = a.getParent();
    assertEquals(Name.parse(""), empty);
    assertEquals(0, empty.size());
    assertEquals(0, empty.length());
    assertEquals((Object)null, empty.getParent());
  }

  @Test
  public void testParse() {
    assertName("");
    assertName("a", "a");
    assertName("a.b", "a", "b");
    assertName("a.b.c", "a", "b", "c");
  }

  private void assertName(String test, String... expected) {
    assertName(expected, "", test, "");
    assertName(expected, "", test, "_");
    assertName(expected, "", test, ".");
    assertName(expected, "", test, "_.");
    assertName(expected, "_", test, "");
    assertName(expected, ".", test, "");
    assertName(expected, "._", test, "");
  }

  private void assertName(String[] expected, String prefix, String s, String suffix) {
    Name name = parse(prefix, s, suffix);
    assertEquals(Arrays.asList(expected), Tools.list(name));
    assertEquals(Tools.join('.', expected), name.toString());
  }

  private void assertIAE(String prefix, String s, String suffix) {
    try {
      parse(prefix, s, suffix);
      fail("Was expecting " + s + " to throw an IAE");
    }
    catch (IllegalArgumentException ok) {
    }
  }

  private Name parse(String prefix, String s, String suffix) {
    return Name.parse(prefix + s + suffix, prefix.length(), prefix.length() + s.length());
  }

  @Test
  public void testResolveDotInRawName() throws Exception {
    Name name = Name.parse("foo");
    Path path = Path.parse("a.b.c");
    Path.Absolute file = name.resolve(path);
    assertEquals("foo", file.getDirs().toString());
    assertEquals("a.b.c", file.getSimpleName());
    assertEquals("b.c", file.ext);
    assertEquals("a", file.getRawName());
    assertEquals("foo.a", file.getName().toString());
  }

  @Test
  public void testResolveUp() throws Exception {
    Name name = Name.parse("foo.bar");
    Path.Absolute file = name.resolve("../juu/daa.txt");
    assertEquals("foo.juu", file.getDirs().toString());
    assertEquals("daa.txt", file.getSimpleName());
    assertEquals("txt", file.getExt());
    assertEquals("daa", file.getRawName());
    assertEquals("foo.juu.daa", file.getName().toString());
  }

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
      Name.parse(value);
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
    Name qn = Name.parse(value);
    ArrayList<String> testNames = Tools.list(qn);
    assertEquals(Arrays.asList(names), testNames);
    assertEquals(value, qn.toString());
  }

  @Test
  public void testEmpty() {
    Name empty = Name.parse("");
    assertEquals(0, empty.size());
    assertEquals(0, empty.length());
    assertEquals(Collections.emptyList(), Tools.list(empty));
    assertNull(empty.getParent());
    assertEquals("", empty.toString());
  }

  @Test
  public void testSimple() {
    Name simple = Name.parse("a");
    assertEquals(1, simple.size());
    assertEquals(1, simple.length());
    assertEquals(Collections.singletonList("a"), Tools.list(simple));
    Name parent = simple.getParent();
    assertEquals(0, parent.size());
    assertEquals(0, parent.length());
    assertEquals("a", simple.toString());
  }

  @Test
  public void testAppend() {
    assertAppend("a.b", "a", "b");
    assertAppend("a.b.c", "a.b", "c");
    try {
      Name.parse("a").append("");
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    try {
      Name.parse("a").append("a.b");
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    try {
      Name.parse("a").append((String)null);
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    try {
      Name.parse("a").append((String[])null);
      fail();
    }
    catch (NullPointerException e) {
    }
  }

  private void assertAppend(String expected, String qn, String simpleName) {
    Name parsed = Name.parse(qn);
    Name appended = parsed.append(simpleName);
    assertEquals(expected, appended.toString());
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
    assertTrue(Name.parse(prefix).isPrefix(Name.parse(s)));
  }

  private void assertNotPrefix(String prefix, String s) {
    assertFalse(Name.parse(prefix).isPrefix(Name.parse(s)));
  }

  @Test
  public void testGetPrefix() {
    assertGetPrefix("a.b", "a.b", "a.b");
    assertGetPrefix("a.b", "a.b.c", "a.b");
    assertGetPrefix("a.b", "a.b.c", "a.b.d");
  }

  private static void assertGetPrefix(String expected, String a, String b) {
    Name actual = Name.parse(a).getPrefix(Name.parse(b));
    assertEquals("Was expecting common prefix between " + a + " and " + b + "  to be equals to " + expected +
        " instead of " + actual,  Name.parse(expected), actual);
  }
}
