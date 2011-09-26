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
package org.juzu.impl.template.parser;

import junit.framework.TestCase;

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
   private TemplateParser parser = new TemplateParser();

   public void testEmpty() throws IOException
   {
      assertEquals(Collections.<TemplateSection>emptyList(), parser.parse(""));
   }

   public void testText() throws IOException
   {
      assertEquals(Arrays.<TemplateSection>asList(new TemplateSection(SectionType.STRING, "a")), parser.parse("a"));
   }

   public void testSingleEmptyScriplet() throws IOException
   {
      assertEquals(Arrays.<TemplateSection>asList(new TemplateSection(SectionType.SCRIPTLET, "")), parser.parse("<%%>"));
   }

   public void testSingleEmptyExpression() throws IOException
   {
      assertEquals(Arrays.<TemplateSection>asList(new TemplateSection(SectionType.EXPR, "")), parser.parse("<%=%>"));
   }

   public void testSingleScriplet() throws IOException
   {
      assertEquals(Arrays.<TemplateSection>asList(new TemplateSection(SectionType.SCRIPTLET, "a")), parser.parse("<%a%>"));
   }

   public void testSingleExpression() throws IOException
   {
      assertEquals(Arrays.<TemplateSection>asList(new TemplateSection(SectionType.EXPR, "a")), parser.parse("<%=a%>"));
   }

   public void testPercentScriplet() throws IOException
   {
      assertEquals(Arrays.<TemplateSection>asList(new TemplateSection(SectionType.SCRIPTLET, "%")), parser.parse("<%%%>"));
   }

   public void testPercentExpression() throws IOException
   {
      assertEquals(Arrays.<TemplateSection>asList(new TemplateSection(SectionType.EXPR, "%")), parser.parse("<%=%%>"));
   }

   public void testStartAngleBracketScriplet() throws IOException
   {
      assertEquals(Arrays.<TemplateSection>asList(new TemplateSection(SectionType.SCRIPTLET, "<")), parser.parse("<%<%>"));
   }

   public void testStartAngleBracketExpression() throws IOException
   {
      assertEquals(Arrays.<TemplateSection>asList(new TemplateSection(SectionType.EXPR, "<")), parser.parse("<%=<%>"));
   }

   public void testSimpleScript() throws IOException
   {
      assertEquals(Arrays.<TemplateSection>asList(
         new TemplateSection(SectionType.STRING, "a"),
         new TemplateSection(SectionType.SCRIPTLET, "b"),
         new TemplateSection(SectionType.STRING, "c")
         ), parser.parse("a<%b%>c"));
   }

   public void testSimpleScript2() throws IOException
   {
      assertEquals(Arrays.<TemplateSection>asList(
         new TemplateSection(SectionType.STRING, "a"),
         new TemplateSection(SectionType.EXPR, "b"),
         new TemplateSection(SectionType.STRING, "c")
         ), parser.parse("a<%=b%>c"));
   }

   public void testWindowsLineBreak() throws IOException
   {

   }

   public void testPosition() throws IOException
   {
      List<TemplateSection> sections = parser.parse("a\nb<%= foo %>d");
      assertEquals(new Location(1, 1), sections.get(0).getItems().get(0).getPosition());
      assertEquals(new Location(2, 1), sections.get(0).getItems().get(1).getPosition());
      assertEquals(new Location(1, 2), sections.get(0).getItems().get(2).getPosition());
      assertEquals(new Location(5, 2), sections.get(1).getItems().get(0).getPosition());
      assertEquals(new Location(12, 2), sections.get(2).getItems().get(0).getPosition());

   }
}
