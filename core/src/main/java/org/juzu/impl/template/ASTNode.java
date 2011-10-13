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

import org.juzu.impl.spi.template.TemplateGenerator;
import org.juzu.utils.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ASTNode
{

   /** . */
   private final Location beginPosition;

   protected ASTNode(Location beginPosition)
   {
      if (beginPosition == null)
      {
         throw new NullPointerException("No null position accepted");
      }
      this.beginPosition = beginPosition;
   }

   public Location getBeginPosition()
   {
      return beginPosition;
   }

   public static class Template extends ASTNode
   {

      /** . */
      private final List<ASTNode.Block> sections;

      Template(List<Block> sections)
      {
         super(new Location(0, 0));

         //
         this.sections = sections;
      }

      public List<Block> getSections()
      {
         return sections;
      }

      public void generate(TemplateGenerator generator)
      {
         GeneratorContext ctx = new GeneratorContext(generator);
         for (ASTNode.Block block : sections)
         {
            if (block instanceof Section)
            {
               Section section = (Section)block;
               ctx.begin(section.getType(), section.getItems().get(0).getBeginPosition());
               for (ASTNode item : section.getItems())
               {
                  ctx.append(item);
               }
               ctx.end();
            }
            else if (block instanceof URL)
            {
               URL url = (URL)block;
               ctx.writer.url(url.name, url.args);
            }
            else
            {
               throw new AssertionError();
            }
         }
      }

      private class GeneratorContext
      {

         /** . */
         private SectionType currentType = null;

         /** . */
         private StringBuilder accumulatedText = new StringBuilder();

         /** . */
         private TemplateGenerator writer;

         GeneratorContext(TemplateGenerator writer)
         {
            this.writer = writer;
         }

         void begin(SectionType sectionType, Location pos)
         {
            if (sectionType == null)
            {
               throw new NullPointerException();
            }
            if (pos == null)
            {
               throw new NullPointerException();
            }
            if (currentType != null)
            {
               throw new IllegalStateException();
            }
            this.currentType = sectionType;

            //
            switch (currentType)
            {
               case STRING:
                  break;
               case SCRIPTLET:
                  writer.startScriptlet(pos);
                  break;
               case EXPR:
                  writer.startExpression(pos);
                  break;
            }
         }

         void append(ASTNode item)
         {
            if (item instanceof ASTNode.Text)
            {
               ASTNode.Text textItem = (ASTNode.Text)item;
               String text = textItem.getData();
               switch (currentType)
               {
                  case STRING:
                     accumulatedText.append(text);
                     break;
                  case SCRIPTLET:
                     writer.appendScriptlet(textItem.getData());
                     break;
                  case EXPR:
                     writer.appendExpression(textItem.getData());
                     break;
               }
            }
            else if (item instanceof ASTNode.LineBreak)
            {
               switch (currentType)
               {
                  case STRING:
                     accumulatedText.append("\n");
                     break;
                  case SCRIPTLET:
                     writer.appendLineBreak(currentType, item.getBeginPosition());
                     break;
                  case EXPR:
                     writer.appendLineBreak(currentType, item.getBeginPosition());
                     break;
               }
            }
            else
            {
               throw new AssertionError();
            }
         }

         void end()
         {
            if (currentType == null)
            {
               throw new IllegalStateException();
            }

            //
            switch (currentType)
            {
               case STRING:
                  if (accumulatedText.length() > 0)
                  {
                     writer.appendText(accumulatedText.toString());
                     accumulatedText.setLength(0);
                  }
                  break;
               case SCRIPTLET:
                  writer.endScriptlet();
                  break;
               case EXPR:
                  writer.endExpression();
                  break;
            }

            //
            this.currentType = null;
         }
      }
   }

   public static class Text extends ASTNode
   {

      /** . */
      private final String data;

      Text(Location pos, String data)
      {
         super(pos);

         //
         if (data == null)
         {
            throw new NullPointerException();
         }

         //
         this.data = data;
      }

      public String getData()
      {
         return data;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
         {
            return true;
         }
         if (obj instanceof Text)
         {
            Text that = (Text)obj;
            return data.equals(that.data);
         }
         return false;
      }

      @Override
      public String toString()
      {
         return getClass().getSimpleName() +  "[pos=" + getBeginPosition() + ",data=" + data + "]";
      }
   }

   public abstract static class Block extends ASTNode
   {

      /** . */
      private final int beginOffset;

      /** . */
      private final int endOffset;

      /** . */
      private Location endPosition;

      protected Block(int beginOffset, int endOffset, Location beginPosition, Location endPosition)
      {
         super(beginPosition);

         //
         this.beginOffset = beginOffset;
         this.endOffset = endOffset;
         this.endPosition = endPosition;
      }

      public int getBeginOffset()
      {
         return beginOffset;
      }

      public int getEndOffset()
      {
         return endOffset;
      }

      public Location getEndPosition()
      {
         return endPosition;
      }
   }

   public static class URL extends Block
   {

      /** . */
      private final String name;

      /** . */
      private final Map<String, String> args;

      public URL(String name, Map<String, String> args)
      {
         this(name, args, 0, 0, new Location(1, 1), new Location(1, 1));
      }

      public URL(String name, Map<String, String> args, int beginOffset, int endOffset, Location beginPosition, Location endPosition)
      {
         super(beginOffset, endOffset, beginPosition, endPosition);

         //
         this.name = name;
         this.args = args;
      }

      public String getName()
      {
         return name;
      }

      public Map<String, String> getArgs()
      {
         return args;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
         {
            return true;
         }
         if (obj instanceof URL)
         {
            URL that = (URL)obj;
            return name.equals(that.name) && args.equals(that.args);
         }
         return false;
      }

      @Override
      public String toString()
      {
         return getClass().getSimpleName() +  "[name=" + name + ",args=" + args + "]";
      }
   }

   public static class Section extends Block
   {

      /** . */
      private final SectionType type;

      /** . */
      private final List<ASTNode> items;

      public Section(SectionType type, String text)
      {
         this(type, 0, 0, text, new Location(1, 1), new Location(1, 1));
      }

      public Section(SectionType type, int beginOffset, int endOffset, String text, Location beginPosition, Location endPosition)
      {
         super(beginOffset, endOffset, beginPosition, endPosition);

         //
         if (type == null)
         {
            throw new NullPointerException();
         }
         if (text == null)
         {
            throw new NullPointerException();
         }

         // Now we process the line breaks
         ArrayList<ASTNode> sections = new ArrayList<ASTNode>();
         int lineNumber = beginPosition.getLine();
         int colNumber = beginPosition.getCol();

         //
         int from = 0;
         while (true)
         {
            int to = text.indexOf('\n', from);

            //
            if (to != -1)
            {
               String chunk = text.substring(from, to);
               sections.add(new Text(new Location(colNumber, lineNumber), chunk));

               //
               sections.add(new LineBreak(new Location(colNumber + (to - from), lineNumber)));

               //
               from = to + 1;
               lineNumber++;
               colNumber = 1;
            }
            else
            {
               String chunk = text.substring(from);
               sections.add(new Text(new Location(colNumber, lineNumber), chunk));
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

      public List<ASTNode> getItems()
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
         if (obj instanceof Section)
         {
            Section that = (Section)obj;
            return type == that.type && items.equals(that.items);
         }
         return false;
      }

      @Override
      public String toString()
      {
         return getClass().getSimpleName() +  "[type=" + type + ",text=" + items + "]";
      }
   }

   public static class LineBreak extends ASTNode
   {

      LineBreak(Location pos)
      {
         super(pos);
      }

      @Override
      public String toString()
      {
         return getClass().getSimpleName() + "[position=" + getBeginPosition() + "]";
      }

      @Override
      public boolean equals(Object obj)
      {
         return obj == this || obj instanceof LineBreak;
      }
   }
}
