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
