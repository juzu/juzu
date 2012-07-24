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
import java.util.Iterator;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ToolsTestCase extends AbstractTestCase {

  @Test
  public void testEmptyNoRecursePackageMatcher() {
    Pattern p = Tools.getPackageMatcher("", false);
    assertTrue(p.matcher("").matches());
    assertFalse(p.matcher("foo").matches());
    assertFalse(p.matcher("foo.bar").matches());
  }

  @Test
  public void testEmptyRecursePackageMatcher() {
    Pattern p = Tools.getPackageMatcher("", true);
    assertTrue(p.matcher("").matches());
    assertTrue(p.matcher("foo").matches());
    assertTrue(p.matcher("foo.bar").matches());
  }

  @Test
  public void testNoRecursePackageMatcher() {
    Pattern p = Tools.getPackageMatcher("foo", false);
    assertTrue(p.matcher("foo").matches());
    assertFalse(p.matcher("bar").matches());
    assertFalse(p.matcher("").matches());
    assertFalse(p.matcher("foo.bar").matches());
    assertFalse(p.matcher("foo.bar.juu").matches());
  }

  @Test
  public void testRecursePackageMatcher() {
    Pattern p = Tools.getPackageMatcher("foo", true);
    assertTrue(p.matcher("foo").matches());
    assertTrue(p.matcher("foo.bar").matches());
    assertTrue(p.matcher("foo.bar.juu").matches());
    assertFalse(p.matcher("").matches());
    assertFalse(p.matcher("bar").matches());
    assertFalse(p.matcher("foobar").matches());
  }

  @Test
  public void testUnquote() {
    assertEquals("", Tools.unquote(""));
    assertEquals("'", Tools.unquote("'"));
    assertEquals("\"", Tools.unquote("\""));
    assertEquals("'\"", Tools.unquote("'\""));
    assertEquals("\"'", Tools.unquote("\"'"));
    assertEquals("", Tools.unquote("''"));
    assertEquals("", Tools.unquote("\"\""));
    assertEquals("a", Tools.unquote("'a'"));
    assertEquals("a", Tools.unquote("\"a\""));
    assertEquals("'a\"", Tools.unquote("'a\""));
    assertEquals("\"a'", Tools.unquote("\"a'"));
  }

  @Test
  public void testCount() {
    assertEquals(0, Tools.count("a", "b"));
    assertEquals(1, Tools.count("a", "a"));
    assertEquals(2, Tools.count("aa", "a"));
    assertEquals(1, Tools.count("", ""));
    assertEquals(2, Tools.count("a", ""));
    assertEquals(3, Tools.count("aa", ""));
    assertEquals(1, Tools.count("aaa", "aa"));
  }

  @Test
  public void testSplit() {
    assertEquals(Collections.<String>emptyList(), Arrays.asList(Tools.split("", '.')));
    assertEquals(Arrays.asList("a"), Arrays.asList(Tools.split("a", '.')));
    assertEquals(Arrays.asList("a", ""), Arrays.asList(Tools.split("a.", '.')));
    assertEquals(Arrays.asList("", "a"), Arrays.asList(Tools.split(".a", '.')));
    assertEquals(Arrays.asList("", ""), Arrays.asList(Tools.split(".", '.')));
    assertEquals(Arrays.asList("a", "b"), Arrays.asList(Tools.split("a.b", '.')));

    //
    String[] ret = Tools.split("a.b", '.', 1);
    assertEquals(3, ret.length);
    assertEquals("a", ret[0]);
    assertEquals("b", ret[1]);
    assertEquals(null, ret[2]);

    //
    ret = Tools.split("", '.', 1);
    assertEquals(1, ret.length);
    assertEquals(null, ret[0]);
  }

  @Test
  public void testIteratorAppend() {
    Iterator i = Tools.append(Collections.emptyList().iterator(), "foo");
    assertTrue(i.hasNext());
    assertEquals("foo", i.next());
    assertFalse(i.hasNext());
    assertNoSuchElement(i);

    //
    i = Tools.append(Collections.singletonList("foo").iterator());
    assertTrue(i.hasNext());
    assertEquals("foo", i.next());
    assertFalse(i.hasNext());
    assertNoSuchElement(i);

    //
    i = Tools.append(Collections.singletonList("foo").iterator(), "bar");
    assertTrue(i.hasNext());
    assertEquals("foo", i.next());
    assertTrue(i.hasNext());
    assertEquals("bar", i.next());
    assertFalse(i.hasNext());
    assertNoSuchElement(i);
  }

  @Test
  public void testTrieEmpty() {
    Trie<String, String> trie = new Trie<String, String>();
    assertNull(trie.getParent());
    assertNull(trie.getKey());
    assertEquals(Collections.emptyList(), Tools.list(trie.getPath()));
    assertNull(trie.value());
    assertNull(trie.value("foo"));
    assertEquals("foo", trie.value());
    assertEquals("foo", trie.value("bar"));
    assertEquals("bar", trie.value());
    assertEquals("bar", trie.value("juu"));
  }

  @Test
  public void testTrieAddKey() {
    Trie<String, String> root = new Trie<String, String>();
    Trie<String, String> foo = root.add("foo");
    assertSame(foo, root.get("foo"));
    assertSame(foo, root.add("foo"));
    assertSame(root, foo.getParent());
  }

  @Test
  public void testTrieAddKeys() {
    Trie<String, String> root = new Trie<String, String>();
    Trie<String, String> bar = root.add("foo", "bar");
    Trie<String, String> foo = bar.getParent();
    assertSame(foo, root.get("foo"));
    assertSame(foo, root.add("foo"));
    assertSame(root, foo.getParent());
    assertSame(bar, foo.get("bar"));
    assertSame(bar, foo.add("bar"));
  }

  @Test
  public void testTrieMerge() {
    Trie<String, String> root1 = new Trie<String, String>();
    Trie<String, String> foo1 = root1.add("foo");
    Trie<String, String> bar1 = foo1.add("bar");

    //
    Trie<String, String> root2 = new Trie<String, String>();
    Trie<String, String> foo2 = root2.add("foo");
    Trie<String, String> juu2 = foo2.add("juu");
    Trie<String, String> daa2 = juu2.add("daa");

    //
    root1.merge(root2);
    assertEquals(Tools.set("foo"), Tools.set(root1));
    assertSame(foo1, root1.get("foo"));
    assertEquals(Tools.set("bar", "juu"), Tools.set(foo1));
    assertSame(bar1, foo1.get("bar"));
    Trie<String, String> juu1 = foo1.get("juu");
    assertNotSame(juu2, juu1);
    assertEquals(Tools.set("daa"), Tools.set(juu1));
    Trie<String, String> boo = juu1.add("boo");
    assertEquals(Tools.set("daa", "boo"), Tools.set(juu1));
    assertEquals(Tools.set("daa"), Tools.set(juu2));
  }
}
