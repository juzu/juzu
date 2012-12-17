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

import org.junit.Test;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.*;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class IntrospectorTestCase {

  @Test
  public void testFoo() {
    Type resolved = Introspector.resolve(ThreadLocal.class, ThreadLocal.class, 0);
    assertTrue(resolved instanceof TypeVariable);
    TypeVariable tv = (TypeVariable)resolved;
    assertEquals(ThreadLocal.class, tv.getGenericDeclaration());
    assertEquals(Object.class, Introspector.resolveToClass(ThreadLocal.class, ThreadLocal.class, 0));
  }

  @Test
  public void testBar() {
    class A extends ThreadLocal<String> {}
    Type resolved = Introspector.resolve(A.class, ThreadLocal.class, 0);
    assertEquals(String.class, resolved);
    assertEquals(String.class, Introspector.resolveToClass(A.class, ThreadLocal.class, 0));
  }

  @Test
  public void testZoo() {
    class A extends ThreadLocal {}
    TypeVariable resolved = (TypeVariable)Introspector.resolve(A.class, ThreadLocal.class, 0);
    assertEquals("T", resolved.getName());
    assertEquals(Collections.<Type>singletonList(Object.class), Arrays.asList(resolved.getBounds()));
    assertEquals(Object.class, Introspector.resolveToClass(A.class, ThreadLocal.class, 0));
  }

  @Test
  public void testJuu() {
    class A extends InheritableThreadLocal<String> {}
    Type resolved = Introspector.resolve(A.class, ThreadLocal.class, 0);
    assertEquals(String.class, resolved);
    assertEquals(String.class, Introspector.resolveToClass(A.class, ThreadLocal.class, 0));
  }

  @Test
  public void testDaa() {
    Type resolved = Introspector.resolve(InheritableThreadLocal.class, ThreadLocal.class, 0);
    assertTrue(resolved instanceof TypeVariable);
    TypeVariable tv = (TypeVariable)resolved;
    assertEquals(InheritableThreadLocal.class, tv.getGenericDeclaration());
    assertEquals(Object.class, Introspector.resolveToClass(InheritableThreadLocal.class, ThreadLocal.class, 0));
  }

  @Test
  public void testInstanceOf() {
    assertEquals(true, Introspector.instanceOf(String.class, "java.lang.String"));
    assertEquals(true, Introspector.instanceOf(ArrayList.class, "java.util.List"));
    assertEquals(true, Introspector.instanceOf(List.class, "java.util.List"));
    assertEquals(true, Introspector.instanceOf(ArrayList.class, "java.util.AbstractList"));
    assertEquals(true, Introspector.instanceOf(ArrayList.class, "java.util.AbstractCollection"));
    assertEquals(false, Introspector.instanceOf(String.class, "java.lang.Integer"));
  }

  @Test
  public void testInstanceOfList() {
    assertEquals(true, Introspector.instanceOf(String.class, Arrays.asList("java.lang.String", "java.lang.Integer")));
    assertEquals(true, Introspector.instanceOf(String.class, Arrays.asList("java.lang.Integer", "java.lang.String")));
    assertEquals(false, Introspector.instanceOf(String.class, Arrays.asList("java.lang.Boolean", "java.lang.Integer")));
  }
}
