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

package juzu.impl.template.spi.juzu.ast;

import juzu.impl.common.Builder;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TemplateParserTestCase extends AbstractTestCase {

  private List<ASTNode.Block<?>> parse(String s) {
    try {
      return ASTNode.Template.parse(s).getChildren();
    }
    catch (juzu.impl.template.spi.juzu.ast.ParseException e) {
      throw failure(e);
    }
  }

  @Test
  public void testEmpty() throws IOException {
    assertEquals(Collections.<ASTNode.Block<?>>emptyList(), parse(""));
  }

  @Test
  public void testText() throws IOException {
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.STRING, "a")), parse("a"));
  }

  @Test
  public void testSingleEmptyScriplet() throws IOException {
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.SCRIPTLET, "")), parse("<%%>"));
  }

  @Test
  public void testSingleEmptyExpression() throws IOException {
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.EXPR, "")), parse("<%=%>"));
  }

  @Test
  public void testSingleScriplet() throws IOException {
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.SCRIPTLET, "a")), parse("<%a%>"));
  }

  @Test
  public void testSingleExpression() throws IOException {
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.EXPR, "a")), parse("<%=a%>"));
  }

  @Test
  public void testPercentScriplet() throws IOException {
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.SCRIPTLET, "%")), parse("<%%%>"));
  }

  @Test
  public void testPercentExpression() throws IOException {
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.EXPR, "%")), parse("<%=%%>"));
  }

  @Test
  public void testStartAngleBracketScriplet() throws IOException {
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.SCRIPTLET, "<")), parse("<%<%>"));
  }

  @Test
  public void testStartAngleBracketExpression() throws IOException {
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.EXPR, "<")), parse("<%=<%>"));
  }

  @Test
  public void testCurlyExpression() throws IOException {
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.EXPR, "a")), parse("${a}"));
  }

  @Test
  public void testMessage() throws IOException {
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Message("")), parse("&{}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Message("a")), parse("&{a}"));
  }

  @Test
  public void testParseURL() throws IOException {
    for (String s : new String[]{"@{a()}","@{ a()}","@{a() }","@{\ta()}","@{a()\t}","@{\ra()}","@{a()\r}","@{\na()}","@{a()\n}"}) {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.<String, String>emptyMap())), parse(s));
    }

    //
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.singletonMap("a", "b"))), parse("@{a(a=b)}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "b").map("c", "d").build())), parse("@{a(a=b,c=d)}"));

    //
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.<String, String>emptyMap())), parse("@{a( )}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.singletonMap("a", "b"))), parse("@{a( a=b)}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.singletonMap("a", "b"))), parse("@{a(a =b)}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.singletonMap("a", "b"))), parse("@{a(a= b)}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.singletonMap("a", "b"))), parse("@{a(a=b )}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "b").map("c", "d").build())), parse("@{a(a=b ,c=d)}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "b").map("c", "d").build())), parse("@{a(a=b, c=d)}"));

    //
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "'b '").build())), parse("@{a(a='b ')}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "'b '").build())), parse("@{a(a= 'b ' )}"));

    //
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "\"b \"").build())), parse("@{a(a=\"b \")}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "\"b \"").build())), parse("@{a(a= \"b \" )}"));

    //
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL("a", "b", Collections.<String, String>emptyMap())), parse("@{a.b()}"));
  }

  @Test
  public void testParseTag() throws IOException {
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo")), parse("#{foo/}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo")), parse("#{foo /}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo", Collections.singletonMap("a", "b"))), parse("#{foo a=b/}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo", Collections.singletonMap("a", "b"))), parse("#{foo a=b /}"));
//      parse("#{foo}");
//      parse("#{foo   }#{/foo}");
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo").addChild(new ASTNode.Section(SectionType.STRING, ""))), parse("#{foo}#{/foo}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo").addChild(new ASTNode.Section(SectionType.STRING, ""))), parse("#{foo }#{/foo}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo").addChild(new ASTNode.Section(SectionType.STRING, ""))), parse("#{foo}#{/foo}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo", Collections.singletonMap("a", "b")).addChild(new ASTNode.Section(SectionType.STRING, ""))), parse("#{foo a=b}#{/foo}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo", Collections.singletonMap("a", "b")).addChild(new ASTNode.Section(SectionType.STRING, ""))), parse("#{foo a =b}#{/foo}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo", Collections.singletonMap("a", "b")).addChild(new ASTNode.Section(SectionType.STRING, ""))), parse("#{foo a= b}#{/foo}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo", Collections.singletonMap("a", "b")).addChild(new ASTNode.Section(SectionType.STRING, ""))), parse("#{foo a=b }#{/foo}"));
    assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo", Collections.singletonMap("a", " ")).addChild(new ASTNode.Section(SectionType.STRING, ""))), parse("#{foo a=' '}#{/foo}"));
  }

  @Test
  public void testParseNestedTag() throws IOException, juzu.impl.template.spi.juzu.ast.ParseException {
    List<ASTNode.Block<?>> o = parse("#{foo} ${bar} #{/foo}");
    List<ASTNode.Block<?>> expected = Collections.<ASTNode.Block<?>>singletonList(
      new ASTNode.Tag("foo").
        addChild(new ASTNode.Section(SectionType.STRING, " ")).
        addChild(new ASTNode.Section(SectionType.EXPR, "bar")).
        addChild(new ASTNode.Section(SectionType.STRING, " "))
    );
    assertEquals(expected, o);
  }

  @Test
  public void testSimpleScript() throws IOException {
    assertEquals(Arrays.<ASTNode.Block<?>>asList(
      new ASTNode.Section(SectionType.STRING, "a"),
      new ASTNode.Section(SectionType.SCRIPTLET, "b"),
      new ASTNode.Section(SectionType.STRING, "c")
    ), parse("a<%b%>c"));
  }

  @Test
  public void testSimpleScript2() throws IOException {
    assertEquals(Arrays.<ASTNode.Block<?>>asList(
      new ASTNode.Section(SectionType.STRING, "a"),
      new ASTNode.Section(SectionType.EXPR, "b"),
      new ASTNode.Section(SectionType.STRING, "c")
    ), parse("a<%=b%>c"));
  }

  @Test
  public void testWindowsLineBreak() throws IOException {

  }
}
