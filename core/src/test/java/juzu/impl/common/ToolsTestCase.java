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
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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
  public void testIterableArray() {
    String[] a = {"a", "b"};
    assertEquals(Collections.<String>emptyList(), Tools.list(Tools.iterable(a, 0, 0)));
    assertEquals(Arrays.asList("a"), Tools.list(Tools.iterable(a, 0, 1)));
    assertEquals(Arrays.asList("a", "b"), Tools.list(Tools.iterable(a, 0, 2)));
    assertEquals(Arrays.asList("b"), Tools.list(Tools.iterable(a, 1, 2)));
  }

  @Test
  public void testSafeConcat() {
    assertEquals(0, Tools.safeConcat(null, null).length);
    assertEquals(Arrays.asList("a"), Arrays.asList(Tools.safeConcat(new String[]{"a"}, null)));
    assertEquals(Arrays.asList("a"), Arrays.asList(Tools.safeConcat(null, new String[]{"a"})));
    assertEquals(Arrays.asList("a", "b"), Arrays.asList(Tools.safeConcat(new String[]{"a"}, new String[]{"b"})));
    assertEquals(Arrays.asList("a", "b", "c"), Arrays.asList(Tools.safeConcat(new String[]{"a", "b"}, new String[]{"c"})));
    assertEquals(Arrays.asList("a", "b", "c"), Arrays.asList(Tools.safeConcat(new String[]{"a"}, new String[]{"b", "c"})));
    assertEquals(Arrays.asList("a", "b", "c", "d"), Arrays.asList(Tools.safeConcat(new String[]{"a", "b"}, new String[]{"c","d"})));
  }

  @Test
  public void testToHex() throws IOException {
    assertEquals("0", Tools.toHex(0, new StringBuilder()).toString());
    assertEquals("1", Tools.toHex(1, new StringBuilder()).toString());
    assertEquals("a", Tools.toHex(10, new StringBuilder()).toString());
    assertEquals("f", Tools.toHex(15, new StringBuilder()).toString());
    assertEquals("10", Tools.toHex(16, new StringBuilder()).toString());
    assertEquals("f0000000", Tools.toHex(0xF0000000, new StringBuilder()).toString());
  }
  
  @Test
  public void testScriptNoAttributes() throws Exception {
    assertSerialization("<script></script>", "<script/>");
  }

  @Test
  public void testScriptWithAttribute() throws Exception {
    assertSerialization("<script type=\"text/javascript\"></script>", "<script type='text/javascript'/>");
  }

  @Test
  public void testMetaNoAttributes() throws Exception {
    assertSerialization("<meta/>", "<meta/>");
  }

  @Test
  public void testMetaWithAttribute() throws Exception {
    assertSerialization("<meta http-equiv=\"Content-Type\"/>", "<meta http-equiv='Content-Type'></meta>");
  }

  @Test
  public void testOrdinaryTextElement() throws Exception {
    assertSerialization("<div>Blah Blah</div>", "<div>Blah Blah</div>");
  }

  private void assertSerialization(String expectedMarkup, String markup) throws Exception {
    Element elt = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        .parse(new InputSource(new StringReader(markup))).getDocumentElement();
    StringWriter writer = new StringWriter();
    Tools.encodeHtml(elt, writer);
    assertEquals(expectedMarkup, writer.toString());
  }
}
