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

package org.juzu.impl.template.ast;

import org.juzu.impl.template.ast.ASTNode;
import org.juzu.impl.template.ast.SectionType;
import org.juzu.impl.utils.Builder;
import org.juzu.test.AbstractTestCase;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TemplateParserTestCase extends AbstractTestCase
{

   private List<ASTNode.Block<?>> parse(String s)
   {
      try
      {
         return ASTNode.Template.parse(s).getChildren();
      }
      catch (org.juzu.impl.template.ast.ParseException e)
      {
         throw failure(e);
      }
   }
   
   public void testEmpty() throws IOException
   {
      assertEquals(Collections.<ASTNode.Block<?>>emptyList(), parse(""));
   }

   public void testText() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.STRING, "a")), parse("a"));
   }

   public void testSingleEmptyScriplet() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.SCRIPTLET, "")), parse("<%%>"));
   }

   public void testSingleEmptyExpression() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.EXPR, "")), parse("<%=%>"));
   }

   public void testSingleScriplet() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.SCRIPTLET, "a")), parse("<%a%>"));
   }

   public void testSingleExpression() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.EXPR, "a")), parse("<%=a%>"));
   }

   public void testPercentScriplet() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.SCRIPTLET, "%")), parse("<%%%>"));
   }

   public void testPercentExpression() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.EXPR, "%")), parse("<%=%%>"));
   }

   public void testStartAngleBracketScriplet() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.SCRIPTLET, "<")), parse("<%<%>"));
   }

   public void testStartAngleBracketExpression() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.EXPR, "<")), parse("<%=<%>"));
   }

   public void testCurlyExpression() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.Section(SectionType.EXPR, "a")), parse("${a}"));
   }

   public void testParseURL() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.<String, String>emptyMap())), parse("@{a()}"));
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.singletonMap("a", "b"))), parse("@{a(a=b)}"));
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "b").put("c", "d").build())), parse("@{a(a=b,c=d)}"));

      //
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.<String, String>emptyMap())), parse("@{a( )}"));
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.singletonMap("a", "b"))), parse("@{a( a=b)}"));
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.singletonMap("a", "b"))), parse("@{a(a =b)}"));
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.singletonMap("a", "b"))), parse("@{a(a= b)}"));
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Collections.singletonMap("a", "b"))), parse("@{a(a=b )}"));
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "b").put("c", "d").build())), parse("@{a(a=b ,c=d)}"));
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "b").put("c", "d").build())), parse("@{a(a=b, c=d)}"));

      //
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "'b '").build())), parse("@{a(a='b ')}"));
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "'b '").build())), parse("@{a(a= 'b ' )}"));

      //
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "\"b \"").build())), parse("@{a(a=\"b \")}"));
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL(null, "a", Builder.map("a", "\"b \"").build())), parse("@{a(a= \"b \" )}"));

      //
      assertEquals(Arrays.<ASTNode.Block<?>>asList(new ASTNode.URL("a", "b", Collections.<String, String>emptyMap())), parse("@{a.b()}"));
   }

   public void testParseTag() throws IOException
   {
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

   public void testParseNestedTag() throws IOException, org.juzu.impl.template.ast.ParseException
   {
      List<ASTNode.Block<?>> o = parse("#{foo} ${bar} #{/foo}");
      List<ASTNode.Block<?>> expected = Collections.<ASTNode.Block<?>>singletonList(
         new ASTNode.Tag("foo").
            addChild(new ASTNode.Section(SectionType.STRING, " ")).
            addChild(new ASTNode.Section(SectionType.EXPR, "bar")).
            addChild(new ASTNode.Section(SectionType.STRING, " "))
      );
      assertEquals(expected, o);
   }

   public void testSimpleScript() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(
         new ASTNode.Section(SectionType.STRING, "a"),
         new ASTNode.Section(SectionType.SCRIPTLET, "b"),
         new ASTNode.Section(SectionType.STRING, "c")
         ), parse("a<%b%>c"));
   }

   public void testSimpleScript2() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Block<?>>asList(
         new ASTNode.Section(SectionType.STRING, "a"),
         new ASTNode.Section(SectionType.EXPR, "b"),
         new ASTNode.Section(SectionType.STRING, "c")
         ), parse("a<%=b%>c"));
   }

   public void testWindowsLineBreak() throws IOException
   {

   }
}
