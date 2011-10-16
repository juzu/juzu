/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.juzu.impl.template;

import junit.framework.TestCase;
import org.juzu.impl.utils.Builder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TemplateParserTestCase extends TestCase
{

   /** . */
   private ASTBuilder parser = new ASTBuilder();

   public void testEmpty() throws IOException
   {
      assertEquals(Collections.<ASTNode.Block<?>>emptyList(), parser.parse("").getChildren());
   }

   public void testText() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.STRING, "a")), parser.parse("a").getChildren());
   }

   public void testSingleEmptyScriplet() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.SCRIPTLET, "")), parser.parse("<%%>").getChildren());
   }

   public void testSingleEmptyExpression() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.EXPR, "")), parser.parse("<%=%>").getChildren());
   }

   public void testSingleScriplet() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.SCRIPTLET, "a")), parser.parse("<%a%>").getChildren());
   }

   public void testSingleExpression() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.EXPR, "a")), parser.parse("<%=a%>").getChildren());
   }

   public void testPercentScriplet() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.SCRIPTLET, "%")), parser.parse("<%%%>").getChildren());
   }

   public void testPercentExpression() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.EXPR, "%")), parser.parse("<%=%%>").getChildren());
   }

   public void testStartAngleBracketScriplet() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.SCRIPTLET, "<")), parser.parse("<%<%>").getChildren());
   }

   public void testStartAngleBracketExpression() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.EXPR, "<")), parser.parse("<%=<%>").getChildren());
   }

   public void testCurlyExpression() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.EXPR, "a")), parser.parse("${a}").getChildren());
   }

   public void testParseURL() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.<String, String>emptyMap())), parser.parse("@{a()}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.singletonMap("a", "b"))), parser.parse("@{a(a=b)}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "b").put("c", "d").build())), parser.parse("@{a(a=b,c=d)}").getChildren());

      //
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.<String, String>emptyMap())), parser.parse("@{a( )}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.singletonMap("a", "b"))), parser.parse("@{a( a=b)}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.singletonMap("a", "b"))), parser.parse("@{a(a =b)}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.singletonMap("a", "b"))), parser.parse("@{a(a= b)}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.singletonMap("a", "b"))), parser.parse("@{a(a=b )}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "b").put("c", "d").build())), parser.parse("@{a(a=b ,c=d)}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "b").put("c", "d").build())), parser.parse("@{a(a=b, c=d)}").getChildren());

      //
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "'b '").build())), parser.parse("@{a(a='b ')}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "'b '").build())), parser.parse("@{a(a= 'b ' )}").getChildren());

      //
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "\"b \"").build())), parser.parse("@{a(a=\"b \")}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "\"b \"").build())), parser.parse("@{a(a= \"b \" )}").getChildren());

      //
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL("a", "b", Collections.<String, String>emptyMap())), parser.parse("@{a.b()}").getChildren());
   }

   public void testParseTag() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo")), parser.parse("#{foo/}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo")), parser.parse("#{foo /}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo", Collections.singletonMap("a", "b"))), parser.parse("#{foo a=b/}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo", Collections.singletonMap("a", "b"))), parser.parse("#{foo a=b /}").getChildren());
//      parser.parse("#{foo}");
//      parser.parse("#{foo   }#{/foo}");
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo").addChild(new ASTNode.Section(SectionType.STRING, ""))), parser.parse("#{foo}#{/foo}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo").addChild(new ASTNode.Section(SectionType.STRING, ""))), parser.parse("#{foo }#{/foo}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo").addChild(new ASTNode.Section(SectionType.STRING, ""))), parser.parse("#{foo}#{/foo}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo", Collections.singletonMap("a", "b")).addChild(new ASTNode.Section(SectionType.STRING, ""))), parser.parse("#{foo a=b}#{/foo}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo", Collections.singletonMap("a", "b")).addChild(new ASTNode.Section(SectionType.STRING, ""))), parser.parse("#{foo a =b}#{/foo}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo", Collections.singletonMap("a", "b")).addChild(new ASTNode.Section(SectionType.STRING, ""))), parser.parse("#{foo a= b}#{/foo}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo", Collections.singletonMap("a", "b")).addChild(new ASTNode.Section(SectionType.STRING, ""))), parser.parse("#{foo a=b }#{/foo}").getChildren());
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Tag("foo", Collections.singletonMap("a", "' '")).addChild(new ASTNode.Section(SectionType.STRING, ""))), parser.parse("#{foo a=' '}#{/foo}").getChildren());
   }

   public void testParseNestedTag() throws IOException
   {
      ASTNode.Template o = parser.parse("#{foo} ${bar} #{/foo}");
      List<ASTNode.Block<?>> expected = Collections.<ASTNode.Block<?>>singletonList(
         new ASTNode.Tag("foo").
            addChild(new ASTNode.Section(SectionType.STRING, " ")).
            addChild(new ASTNode.Section(SectionType.EXPR, "bar")).
            addChild(new ASTNode.Section(SectionType.STRING, " "))
      );
      assertEquals(expected, o.getChildren());
   }

   public void testSimpleScript() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(
         new ASTNode.Section(SectionType.STRING, "a"),
         new ASTNode.Section(SectionType.SCRIPTLET, "b"),
         new ASTNode.Section(SectionType.STRING, "c")
         ), parser.parse("a<%b%>c").getChildren());
   }

   public void testSimpleScript2() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(
         new ASTNode.Section(SectionType.STRING, "a"),
         new ASTNode.Section(SectionType.EXPR, "b"),
         new ASTNode.Section(SectionType.STRING, "c")
         ), parser.parse("a<%=b%>c").getChildren());
   }

   public void testWindowsLineBreak() throws IOException
   {

   }
}
