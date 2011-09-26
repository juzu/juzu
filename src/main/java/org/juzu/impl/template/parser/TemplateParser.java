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

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateParser
{

   private enum Status
   {
      TEXT,

      EXPR,

      SCRIPTLET,

      START_ANGLE,

      SCRIPLET_OR_EXPR,

      MAYBE_SCRIPLET_END,

      MAYBE_EXPR_END,

      MAYBE_GSTRING_EXPR,

      GSTRING_CURLY_EXPR,

      GSTRING_EXPR,

      BACKSLASH
   }

   public List<TemplateSection> parse(String s)
   {
      try
      {
         return parse(new StringReader(s));
      }
      catch (IOException e)
      {
         throw new UnsupportedOperationException(e);
      }
   }

   public List<TemplateSection> parse(Reader tmp) throws IOException
   {
      PushbackReader reader = new PushbackReader(tmp);


      //
      ArrayList<TemplateSection> sections = new ArrayList<TemplateSection>();
      StringBuilder accumulator = new StringBuilder();

      //
      int lineNumber = 1;
      int colNumber = 1;
      Location pos = new Location(1, 1);
      Status status = Status.TEXT;
      int i;
      while ((i = reader.read()) != -1)
      {
         char c = (char)i;

         //
         if (c == '\r')
         {
            // On Windows, "\r\n" is a new line
            int j = reader.read();
            if (j != -1)
            {
               char c2 = (char)j;
               if (c2 == '\n')
               {
                  c = '\n';
               }
               else
               {
                  reader.unread(j);
               }
            }
         }

         // Update current position
         if (c == '\n')
         {
            colNumber = 1;
            lineNumber++;
         }
         else
         {
            colNumber++;
         }

         //
         switch (status)
         {
            case TEXT:
               if (c == '<')
               {
                  status = Status.START_ANGLE;
               }
               else if (c == '\\')
               {
                  status = Status.BACKSLASH;
               }
               else if (c == '$')
               {
                  status = Status.MAYBE_GSTRING_EXPR;
               }
               else
               {
                  accumulator.append(c);
               }
               break;
            case EXPR:
               if (c == '%')
               {
                  status = Status.MAYBE_EXPR_END;
               }
               else
               {
                  accumulator.append(c);
               }
               break;
            case SCRIPTLET:
               if (c == '%')
               {
                  status = Status.MAYBE_SCRIPLET_END;
               }
               else
               {
                  accumulator.append(c);
               }
               break;
            case START_ANGLE:
               if (c == '%')
               {
                  status = Status.SCRIPLET_OR_EXPR;
               }
               else
               {
                  status = Status.TEXT;
                  accumulator.append('<').append(c);
               }
               break;
            case SCRIPLET_OR_EXPR:
               if (accumulator.length() > 0)
               {
                  sections.add(new TemplateSection(SectionType.STRING, accumulator.toString(), pos));
                  accumulator.setLength(0);
                  pos = new Location(colNumber, lineNumber);
               }
               if (c == '=')
               {
                  status = Status.EXPR;
               }
               else if (c == '%')
               {
                  status = Status.MAYBE_SCRIPLET_END;
               }
               else
               {
                  status = Status.SCRIPTLET;
                  accumulator.append(c);
               }
               break;
            case MAYBE_SCRIPLET_END:
               if (c == '>')
               {
                  sections.add(new TemplateSection(SectionType.SCRIPTLET, accumulator.toString(), pos));
                  accumulator.setLength(0);
                  pos = new Location(colNumber, lineNumber);

                  //
                  status = Status.TEXT;
               }
               else if (c == '%')
               {
                  accumulator.append('%');
               }
               else
               {
                  status = Status.SCRIPTLET;
                  accumulator.append('%').append(c);
               }
               break;
            case MAYBE_EXPR_END:
               if (c == '>')
               {
                  sections.add(new TemplateSection(SectionType.EXPR, accumulator.toString(), pos));
                  accumulator.setLength(0);
                  pos = new Location(colNumber, lineNumber);

                  //
                  status = Status.TEXT;
               }
               else if (c == '%')
               {
                  accumulator.append('%');
               }
               else
               {
                  status = Status.EXPR;
                  accumulator.append('%').append(c);
               }
               break;
            case MAYBE_GSTRING_EXPR:
               if (c == '{')
               {
                  if (accumulator.length() > 0)
                  {
                     sections.add(new TemplateSection(SectionType.STRING, accumulator.toString(), pos));
                     accumulator.setLength(0);
                     pos = new Location(colNumber, lineNumber);
                  }
                  status = Status.GSTRING_CURLY_EXPR;
               }
               else if (Character.isJavaIdentifierStart(c))
               {
                  if (accumulator.length() > 0)
                  {
                     sections.add(new TemplateSection(SectionType.STRING, accumulator.toString(), pos));
                     accumulator.setLength(0);
                     pos = new Location(colNumber, lineNumber);
                  }
                  status = Status.GSTRING_EXPR;
                  accumulator.append(c);
               }
               else
               {
                  accumulator.append('$').append(c);
               }
               break;
            case GSTRING_CURLY_EXPR:
               if (c == '}')
               {
                  sections.add(new TemplateSection(SectionType.EXPR, accumulator.toString(), pos));
                  accumulator.setLength(0);
                  pos = new Location(colNumber, lineNumber);

                  //
                  status = Status.TEXT;
               }
               else
               {
                  accumulator.append(c);
               }
               break;
            case GSTRING_EXPR:
               if (c == '.' || Character.isJavaIdentifierPart(c))
               {
                  accumulator.append(c);
               }
               else
               {
                  sections.add(new TemplateSection(SectionType.EXPR, accumulator.toString(), pos));
                  accumulator.setLength(0);
                  pos = new Location(colNumber, lineNumber);

                  //
                  status = Status.TEXT;
                  accumulator.append(c);
               }
               break;
            case BACKSLASH:
               accumulator.append('\\');
               accumulator.append(c);
               status = Status.TEXT;
               break;
            default:
               throw new AssertionError();
         }
      }

      // Last section
      if (accumulator.length() > 0)
      {
         switch (status)
         {
            case TEXT:
               sections.add(new TemplateSection(SectionType.STRING, accumulator.toString(), pos));
               accumulator.setLength(0);
               pos = new Location(colNumber, lineNumber);
               break;
            case GSTRING_EXPR:
               sections.add(new TemplateSection(SectionType.EXPR, accumulator.toString(), pos));
               accumulator.setLength(0);
               pos = new Location(colNumber, lineNumber);
               break;
            default:
               throw new AssertionError();
         }
      }

      //
      return Collections.unmodifiableList(sections);
   }
}