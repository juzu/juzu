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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateSection
{

   /** . */
   private final SectionType type;

   /** . */
   private final List<SectionItem> items;

   public TemplateSection(SectionType type, String text)
   {
      this(type, text, 0, 0);
   }

   public TemplateSection(SectionType type, String text, Location pos)
   {
      this(type, text, pos.getCol(), pos.getLine());
   }

   public TemplateSection(SectionType type, String text, int colNumber, int lineNumber)
   {
      if (type == null)
      {
         throw new NullPointerException();
      }
      if (text == null)
      {
         throw new NullPointerException();
      }

      // Now we process the line breaks
      ArrayList<SectionItem> sections = new ArrayList<SectionItem>();

      //
      int from = 0;
      while (true)
      {
         int to = text.indexOf('\n', from);

         //
         if (to != -1)
         {
            String chunk = text.substring(from, to);
            sections.add(new TextItem(new Location(colNumber, lineNumber), chunk));

            //
            sections.add(new LineBreakItem(new Location(colNumber + (to - from), lineNumber)));

            //
            from = to + 1;
            lineNumber++;
            colNumber = 1;
         }
         else
         {
            String chunk = text.substring(from);
            sections.add(new TextItem(new Location(colNumber, lineNumber), chunk));
            break;
         }
      }

      //
      this.type = type;
      this.items = Collections.unmodifiableList(sections);
   }

   public SectionType getType()
   {
      return type;
   }

   public List<SectionItem> getItems()
   {
      return items;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof TemplateSection)
      {
         TemplateSection that = (TemplateSection)obj;
         return type == that.type && items.equals(that.items);
      }
      return false;
   }

   @Override
   public String toString()
   {
      return "TextSection[type=" + type + ",text=" + items + "]";
   }
}
