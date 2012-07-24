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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JSONTestCase extends AbstractTestCase {

  @Test
  public void testReadMap() throws Exception {
    JSON o = (JSON)JSON.parse("{\"a\":\"b\"}");
    assertEquals(Collections.singleton("a"), o.names());
    assertEquals("b", o.get("a"));
  }

  @Test
  public void testWriteMap() throws Exception {
    assertEquals("{\"a\":\"a_value\",\"b\":2}", JSON.toString(new JSON().set("a", "a_value").set("b", 2), new StringBuilder()).toString());
  }

  @Test
  public void testReadArray() throws Exception {
    List<?> o = (List<?>)JSON.parse("[\"a\",\"b\"]");
    assertEquals(2, o.size());
    assertEquals("a", o.get(0));
    assertEquals("b", o.get(1));
  }

  @Test
  public void testWriteArray() throws Exception {
    assertEquals("[0]", JSON.toString(Arrays.asList(0), new StringBuilder()).toString());
  }

  @Test
  public void testReadString() throws Exception {
    assertEquals("abc", (String)JSON.parse("\"abc\""));
    assertEquals("abc", (String)JSON.parse("'abc'"));
    assertEquals("\"", (String)JSON.parse("\"\\\"\""));
    assertEquals("'", (String)JSON.parse("\"'\""));
    assertEquals("\n", (String)JSON.parse("\"\\n\""));
    assertEquals("\r", (String)JSON.parse("\"\\r\""));
    assertEquals("\b", (String)JSON.parse("\"\\b\""));
    assertEquals("\f", (String)JSON.parse("\"\\f\""));
    assertEquals("\t", (String)JSON.parse("\"\\t\""));
    assertEquals("\\", (String)JSON.parse("\"\\\\\""));
  }

  @Test
  public void testWriteString() throws Exception {
    assertEquals("\"a\"", JSON.toString("a", new StringBuilder()).toString());
    assertEquals("\"\\\"\"", JSON.toString("\"", new StringBuilder()).toString());
    assertEquals("\"\\n\"", JSON.toString("\n", new StringBuilder()).toString());
    assertEquals("\"\\r\"", JSON.toString("\r", new StringBuilder()).toString());
    assertEquals("\"\\b\"", JSON.toString("\b", new StringBuilder()).toString());
    assertEquals("\"\\f\"", JSON.toString("\f", new StringBuilder()).toString());
    assertEquals("\"\\t\"", JSON.toString("\t", new StringBuilder()).toString());
  }

  @Test
  public void testReadBoolean() throws Exception {
    assertEquals(true, JSON.parse("true"));
    assertEquals(false, JSON.parse("false"));
  }

  @Test
  public void testWriteBoolean() throws Exception {
    assertEquals("true", JSON.toString(true, new StringBuilder()).toString());
    assertEquals("false", JSON.toString(false, new StringBuilder()).toString());
  }

  @Test
  public void testReadNumber() throws Exception {
    assertEquals(123, JSON.parse("123"));
  }

  @Test
  public void testWriteNumber() throws Exception {
    assertEquals("0", JSON.toString(0, new StringBuilder()).toString());
    assertEquals("0", JSON.toString(0L, new StringBuilder()).toString());
  }

  @Test
  public void testToJSON() throws Exception {
    class Foo {
      final String value;

      Foo(String value) {
        this.value = value;
      }

      public JSON toJSON() {
        return new JSON().set("value", value);
      }
    }
    JSON json = new JSON().set("foo", new Foo("bar"));
    assertEquals(new JSON().set("value", "bar"), json.getJSON("foo"));
  }

  @Test
  public void testUnwrapArray() throws Exception {
    JSON json = new JSON().set("foo", (Object)new String[]{"bar_1", "bar_2"});
    assertEquals(Arrays.asList("bar_1", "bar_2"), json.getList("foo"));
  }

  @Test
  public void testCastToString() throws Exception {
    assertEquals("bar", new JSON().set("foo", "bar").getString("foo"));
    assertNull(new JSON().getString("foo"));
    try {
      new JSON().set("foo", true).getString("foo");
      fail();
    }
    catch (ClassCastException ignore) {
    }
  }

  @Test
  public void testCastToList() throws Exception {
    assertEquals(Arrays.asList("bar"), new JSON().map("foo", Arrays.asList("bar")).getList("foo"));
    assertEquals(Arrays.asList("bar"), new JSON().map("foo", Arrays.asList("bar")).getList("foo", String.class));
    assertNull(new JSON().getList("foo"));
    assertNull(new JSON().getList("foo", Boolean.class));
    try {
      new JSON().set("foo", true).getList("foo");
      fail();
    }
    catch (ClassCastException ignore) {
    }
    try {
      new JSON().set("foo", true).getList("foo", String.class);
      fail();
    }
    catch (ClassCastException ignore) {
    }
    try {
      new JSON().map("foo", Arrays.asList("String")).getList("foo", Boolean.class);
      fail();
    }
    catch (ClassCastException ignore) {
    }
  }

  @Test
  public void testCastToJSON() throws Exception {
    assertEquals(new JSON(), new JSON().set("foo", new JSON()).getJSON("foo"));
    try {
      new JSON().set("foo", true).getJSON("foo");
      fail();
    }
    catch (ClassCastException ignore) {
    }
  }

  @Test
  public void testNull() throws Exception {
    JSON json = new JSON().set("foo", null);
    String s = json.toString();
    assertEquals("{\"foo\":null}", s);
    JSON unmarshalled = (JSON)JSON.parse(s);
    assertEquals(json, unmarshalled);
  }

  @Test
  public void testParseArray() throws Exception {
    String s = "{\"a\":[{\"b\":\"c\"}]}";
    JSON json = (JSON)JSON.parse(s);
    JSON expected = new JSON().set("a", Arrays.asList(new JSON().set("b", "c")));
    assertEquals(expected, json);
  }

  @Test
  public void testWriteIndented() throws Exception {
    assertEquals("{\n" +
      "  \"a\":\"a_value\",\n" +
      "  \"b\":2,\n" +
      "  \"c\":[1,2,3],\n" +
      "  \"d\":{\n" +
      "    \"e\":true\n" +
      "  }\n" +
      "}", JSON.toString(new JSON().set("a", "a_value").set("b", 2).list("c", 1, 2, 3).set("d", new JSON().set("e", true)), new StringBuilder(), 2).toString());
  }
}
