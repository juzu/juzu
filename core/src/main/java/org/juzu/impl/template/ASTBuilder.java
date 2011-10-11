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

import org.apache.commons.io.input.CharSequenceReader;
import org.juzu.utils.Location;

import java.util.ArrayList;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ASTBuilder
{

   public ASTNode.Template parse(CharSequence s)
   {
      return build(new CharSequenceReader(s));
   }

   private ASTNode.Template build(CharSequenceReader reader)
   {

      ArrayList<ASTNode.Section> sections = new ArrayList<ASTNode.Section>();

      TemplateParser simple = new TemplateParser(reader);

      Token token;
      try
      {
         token = simple.parse();
      }
      catch (ParseException e)
      {
         // todo
         throw new AssertionError(e);
      }

      //
      StringBuilder accumulator = new StringBuilder();
      Location pos = null;

      //
      for (;token != null;token = token.next)
      {
         switch (token.kind)
         {

            //
            case TemplateParserConstants.EOF:
               break;

            //
            case TemplateParserConstants.DATA:
               if (pos == null)
               {
                  pos = new Location(token.beginColumn, token.beginLine);
               }
               accumulator.append(token.image.charAt(0));
               break;

            //
            case TemplateParserConstants.OPEN_EXPR:
            case TemplateParserConstants.OPEN_CURLY_EXPR:
            case TemplateParserConstants.OPEN_SCRIPTLET:
               if (accumulator.length()  > 0)
               {
                  sections.add(new ASTNode.Section(SectionType.STRING, accumulator.toString(), pos));
                  accumulator.setLength(0);
               }
               pos = new Location(token.beginColumn, token.beginLine);
               break;

            //
            case TemplateParserConstants.EXPR_DATA:
            case TemplateParserConstants.CURLY_EXPR_DATA:
            case TemplateParserConstants.SCRIPTLET_DATA:
               accumulator.append(token.image.charAt(0));
               break;

            //
            case TemplateParserConstants.CLOSE_EXPR:
            case TemplateParserConstants.CLOSE_CURLY_EXPR:
               sections.add(new ASTNode.Section(SectionType.EXPR, accumulator.toString(), pos));
               accumulator.setLength(0);
               pos = null;
               break;

            //
            case TemplateParserConstants.CLOSE_SCRIPTLET:
               sections.add(new ASTNode.Section(SectionType.SCRIPTLET, accumulator.toString(), pos));
               accumulator.setLength(0);
               pos = null;
               break;

            //
            default:
               throw new AssertionError("Unexpected kind " + token.kind);
         }
      }

      //
      if (accumulator.length()  > 0)
      {
         sections.add(new ASTNode.Section(SectionType.STRING, accumulator.toString(), pos));
         accumulator.setLength(0);
      }

     //
      return new ASTNode.Template(Collections.unmodifiableList(sections));
   }
}