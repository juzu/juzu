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
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ASTBuilder
{

   public ASTNode.Template parse(CharSequence s)
   {
      return build(s, new CharSequenceReader(s));
   }

   private ASTNode.Template build(CharSequence s, CharSequenceReader reader)
   {

      //
      TemplateParser parser = new TemplateParser(new OffsetTokenManager(new OffsetCharStream(reader)));

      //
      try
      {
         parser.parse();
      }
      catch (ParseException e)
      {
         // todo
         throw new AssertionError(e);
      }

      //
      List<ASTNode.Section> sections = new ArrayList<ASTNode.Section>();
      int previousOffset = 0;
      Location previousPosition = new Location(1, 1);
      for (int i = 0;i < parser.list.size();i++)
      {
         ASTNode.Section section = parser.list.get(i);
         if (section.getBeginOffset() > previousOffset)
         {
            sections.add(new ASTNode.Section(
               SectionType.STRING,
               previousOffset,
               section.getBeginOffset(),
               s.subSequence(previousOffset, section.getBeginOffset()).toString(),
               previousPosition,
               section.getEndPosition()));
         }
         sections.add(section);
         previousOffset = section.getEndOffset();
         previousPosition = section.getEndPosition();
      }
      if (previousOffset < s.length())
      {
         sections.add(new ASTNode.Section(
            SectionType.STRING,
            previousOffset,
            s.length(),
            s.subSequence(previousOffset, s.length()).toString(),
            previousPosition,
            new Location(parser.token.endColumn, parser.token.endLine)));
      }

      //
      return new ASTNode.Template(Collections.unmodifiableList(sections));
   }
}