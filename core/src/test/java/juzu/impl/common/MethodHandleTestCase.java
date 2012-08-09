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

import java.lang.reflect.Method;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MethodHandleTestCase extends AbstractTestCase {

  @Test
  public void testToString() {
    assertEquals("a#b()", new MethodHandle("a", "b").toString());
    assertEquals("a#b(c)", new MethodHandle("a", "b", "c").toString());
    assertEquals("a#b(c,d)", new MethodHandle("a", "b", "c", "d").toString());
  }

  @Test
  public void testInvalid() {
    String[] a = {
        "a",
        "a#",
        "a#b",
        "a#b(",
        "a#b(c",
        "a#b(,)",
    };
    for (String s : a) {
      try {
        MethodHandle.parse(s);
        fail();
      }
      catch (IllegalArgumentException ignore) {
      }
    }
  }

  @Test
  public void testParse() {
    assertEquals(new MethodHandle("a", "b"), MethodHandle.parse("a#b()"));
    assertEquals(new MethodHandle("a", "b", "c"), MethodHandle.parse("a#b(c)"));
    assertEquals(new MethodHandle("a", "b", "c", "d"), MethodHandle.parse("a#b(c,d)"));
  }

  @Test
  public void testJavaLangReflectMethod() {
    assertParameterTypes(getMethod(new Object(){ public void m(String p) {}}), "java.lang.String");
    assertParameterTypes(getMethod(new Object(){ public void m(String[] p) {}}), "java.lang.String[]");
    assertParameterTypes(getMethod(new Object(){ public void m(String[][] p) {}}), "java.lang.String[][]");
    assertParameterTypes(getMethod(new Object(){ public void m(List<String> p) {}}), "java.util.List<java.lang.String>");
    assertParameterTypes(getMethod(new Object(){ public void m(List<String>[] p) {}}), "java.util.List<java.lang.String>[]");
  }

  private static void assertParameterTypes(Method m, String... parameterTypes) {
    MethodHandle handle = new MethodHandle(m);
    assertEquals(parameterTypes.length, handle.getParameterSize());
    for (int i = 0;i < parameterTypes.length;i++) {
      assertEquals(parameterTypes[i], handle.getParameterAt(i));
    }
  }

  private static Method getMethod(Object o) {
    return o.getClass().getDeclaredMethods()[0];
  }
}
