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

package juzu.impl.router;

import juzu.test.AbstractTestCase;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PathTestCase extends AbstractTestCase {

  @Test
  public void testFoo() throws Exception {
    assertEquals("", Path.parse("").getValue());
    assertEquals("a", Path.parse("a").getValue());
    assertEquals("?", Path.parse("%3F").getValue());
    assertEquals(" ", Path.parse("%20").getValue());
    assertEquals("? ", Path.parse("%3F%20").getValue());

    //
    Path p2 = Path.parse("_");
    assertEquals("_", p2.getValue());
    assertFalse(p2.isEscaped(0));

    //
    Path p3 = Path.parse("a%5Fb%5Fc");
    assertEquals("a_b_c", p3.getValue());
    assertFalse(p3.isEscaped(0));
    assertTrue(p3.isEscaped(1));
    assertFalse(p3.isEscaped(2));
    assertTrue(p3.isEscaped(3));
    assertFalse(p3.isEscaped(4));

    //
    Path p4 = p3.subPath(2);
    assertFalse(p4.isEscaped(0));
    assertTrue(p4.isEscaped(1));
    assertFalse(p4.isEscaped(2));
  }

  @Test
  public void testOtherChar() {
    assertInvalid("é");
  }

  @Test
  public void testPercent1() {
    Path path = Path.parse("%5F");
    assertEquals("_", path.getValue());
    assertTrue(path.isEscaped(0));
  }

  @Test
  public void testPercent2() {
    Path path = Path.parse("%C2%A2");
    assertEquals(1, path.length());
    assertEquals('\u00A2', path.charAt(0));
    assertTrue(path.isEscaped(0));
  }

  @Test
  public void testPercent3() {
    Path path = Path.parse("%E2%82%AC");
    assertEquals(1, path.length());
    assertEquals('\u20AC', path.charAt(0));
    assertTrue(path.isEscaped(0));
  }

  @Test
  public void testSubPath() {
    Path path = Path.parse("foo");
    assertEquals("foo", path.subPath(0).getValue());
    assertEquals("oo", path.subPath(1).getValue());
    assertEquals("o", path.subPath(2).getValue());
    assertEquals("", path.subPath(3).getValue());
    try {
      path.subPath(4);
      fail();
    }
    catch (IndexOutOfBoundsException expected) {
    }
    try {
      path.subPath(-1);
      fail();
    }
    catch (IndexOutOfBoundsException expected) {
    }
  }

  @Test
  public void testInvalid() {
    // Not enough chars
    assertInvalid("%");

    // Third char should be hexadecimal value
    assertInvalid("%1z");

    // '_' should be '%'
    assertInvalid("%C2_A2");

    // Not enough chars
    assertInvalid("%C2%A");

    // Corrupted prefix 0xFF is illegal
    assertInvalid("%FF");
  }

  private void assertInvalid(String s) {
    try {
      Path.parse(s);
      fail("Was expecting " + s + " to be invalid");
    }
    catch (IllegalArgumentException ignore) {
    }
  }
}
