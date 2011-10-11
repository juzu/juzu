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
import org.juzu.utils.Location;

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
      assertEquals(Collections.<ASTNode.Section>emptyList(), parser.parse("").getSections());
   }

   public void testText() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Section>asList(new ASTNode.Section(SectionType.STRING, "a")), parser.parse("a").getSections());
   }

   public void testSingleEmptyScriplet() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Section>asList(new ASTNode.Section(SectionType.SCRIPTLET, "")), parser.parse("<%%>").getSections());
   }

   public void testSingleEmptyExpression() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Section>asList(new ASTNode.Section(SectionType.EXPR, "")), parser.parse("<%=%>").getSections());
   }

   public void testSingleScriplet() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Section>asList(new ASTNode.Section(SectionType.SCRIPTLET, "a")), parser.parse("<%a%>").getSections());
   }

   public void testSingleExpression() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Section>asList(new ASTNode.Section(SectionType.EXPR, "a")), parser.parse("<%=a%>").getSections());
   }

   public void testPercentScriplet() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Section>asList(new ASTNode.Section(SectionType.SCRIPTLET, "%")), parser.parse("<%%%>").getSections());
   }

   public void testPercentExpression() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Section>asList(new ASTNode.Section(SectionType.EXPR, "%")), parser.parse("<%=%%>").getSections());
   }

   public void testStartAngleBracketScriplet() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Section>asList(new ASTNode.Section(SectionType.SCRIPTLET, "<")), parser.parse("<%<%>").getSections());
   }

   public void testStartAngleBracketExpression() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Section>asList(new ASTNode.Section(SectionType.EXPR, "<")), parser.parse("<%=<%>").getSections());
   }

   public void testCurlyExpression() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Section>asList(new ASTNode.Section(SectionType.EXPR, "a")), parser.parse("${a}").getSections());
   }

   public void testSimpleScript() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Section>asList(
         new ASTNode.Section(SectionType.STRING, "a"),
         new ASTNode.Section(SectionType.SCRIPTLET, "b"),
         new ASTNode.Section(SectionType.STRING, "c")
         ), parser.parse("a<%b%>c").getSections());
   }

   public void testSimpleScript2() throws IOException
   {
      assertEquals(Arrays.<ASTNode.Section>asList(
         new ASTNode.Section(SectionType.STRING, "a"),
         new ASTNode.Section(SectionType.EXPR, "b"),
         new ASTNode.Section(SectionType.STRING, "c")
         ), parser.parse("a<%=b%>c").getSections());
   }

   public void testWindowsLineBreak() throws IOException
   {

   }

   public void testPosition() throws IOException
   {
      List<ASTNode.Section> sections = parser.parse("a\nb<%= foo %>d").getSections();
      assertEquals(new Location(1, 1), sections.get(0).getItems().get(0).getPosition());
      assertEquals(new Location(2, 1), sections.get(0).getItems().get(1).getPosition());
      assertEquals(new Location(1, 2), sections.get(0).getItems().get(2).getPosition());
      assertEquals(new Location(2, 2), sections.get(1).getItems().get(0).getPosition());
      assertEquals(new Location(12, 2), sections.get(2).getItems().get(0).getPosition());

   }
}
