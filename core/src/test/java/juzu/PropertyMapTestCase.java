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

package juzu;

import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.util.Arrays;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PropertyMapTestCase extends AbstractTestCase {

  /** . */
  public static final PropertyType<String> FOO = new PropertyType<String>() {
  };

  @Test
  public void testEmpty() {
    PropertyMap pm = new PropertyMap();
    assertNull(pm.getValue(FOO));
    assertNull(pm.getValues(FOO));
    assertFalse(pm.contains(FOO));
  }

  @Test
  public void testSingleValue() {
    PropertyMap pm = new PropertyMap();
    pm.setValue(FOO, "1");
    assertEquals("1", pm.getValue(FOO));
    assertEquals(Arrays.asList("1"), pm.getValues(FOO));
    assertTrue(pm.contains(FOO));
  }

  @Test
  public void testMultiValue() {
    PropertyMap pm = new PropertyMap();
    pm.setValues(FOO, "1", "2");
    assertEquals("1", pm.getValue(FOO));
    assertEquals(Arrays.asList("1", "2"), pm.getValues(FOO));
    assertTrue(pm.contains(FOO));
  }

  @Test
  public void testAddValue() {
    PropertyMap pm = new PropertyMap();
    pm.addValue(FOO, "1");
    assertEquals("1", pm.getValue(FOO));
    assertEquals(Arrays.asList("1"), pm.getValues(FOO));
    pm.addValue(FOO, "2");
    assertEquals("1", pm.getValue(FOO));
    assertEquals(Arrays.asList("1", "2"), pm.getValues(FOO));
    assertTrue(pm.contains(FOO));
  }

  @Test
  public void testRemoveEmptyValue() {
    PropertyMap pm = new PropertyMap();
    pm.remove(FOO);
    assertNull(pm.getValue(FOO));
    assertFalse(pm.contains(FOO));
  }

  @Test
  public void testRemoveSingleValue() {
    PropertyMap pm = new PropertyMap();
    pm.setValue(FOO, "1");
    pm.remove(FOO);
    assertNull(pm.getValue(FOO));
    assertFalse(pm.contains(FOO));
  }

  @Test
  public void testRemoveMultiValue() {
    PropertyMap pm = new PropertyMap();
    pm.setValues(FOO, "1", "2");
    pm.remove(FOO);
    assertNull(pm.getValue(FOO));
    assertFalse(pm.contains(FOO));
  }

  @Test
  public void testSingleValueDelegate() {
    PropertyMap pm1 = new PropertyMap();
    PropertyMap pm2 = new PropertyMap(pm1);
    pm1.setValue(FOO, "1");
    assertEquals("1", pm2.getValue(FOO));
    pm2.setValue(FOO, "2");
    assertEquals("1", pm1.getValue(FOO));
    assertEquals("2", pm2.getValue(FOO));
  }

  @Test
  public void testMultiValueDelegate() {
    PropertyMap pm1 = new PropertyMap();
    PropertyMap pm2 = new PropertyMap(pm1);
    pm1.setValues(FOO, "1", "2");
    assertEquals(Arrays.asList("1", "2"), pm2.getValues(FOO));
    pm2.setValue(FOO, "3");
    assertEquals(Arrays.asList("1", "2"), pm1.getValues(FOO));
    assertEquals(Arrays.asList("3"), pm2.getValues(FOO));
  }

  @Test
  public void testAddValueDelegate() {
    PropertyMap pm1 = new PropertyMap();
    PropertyMap pm2 = new PropertyMap(pm1);
    pm1.setValues(FOO, "1");
    pm2.addValue(FOO, "2");
    assertEquals(Arrays.asList("1"), pm1.getValues(FOO));
    assertEquals(Arrays.asList("1", "2"), pm2.getValues(FOO));
  }

  @Test
  public void testRemoveValueDelegate() {
    PropertyMap pm1 = new PropertyMap();
    PropertyMap pm2 = new PropertyMap(pm1);
    pm1.setValues(FOO, "1");
    pm2.remove(FOO);
    assertEquals(Arrays.asList("1"), pm1.getValues(FOO));
    assertNull(pm2.getValues(FOO));
  }
}
