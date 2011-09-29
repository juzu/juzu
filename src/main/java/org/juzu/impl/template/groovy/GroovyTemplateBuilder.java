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

package org.juzu.impl.template.groovy;

import org.juzu.impl.template.ASTNode;
import org.juzu.impl.template.SectionType;
import org.juzu.impl.template.TemplateBuilder;
import org.juzu.template.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class GroovyTemplateBuilder extends TemplateBuilder<GroovyTemplate>
{

   /** . */
   private final String templateId;

   /** . */
   private StringBuilder out = new StringBuilder();

   /** . */
   private List<TextConstant> textMethods = new ArrayList<TextConstant>();

   /** . */
   private int methodCount = 0;

   /** The line number table. */
   private HashMap<Integer, ASTNode.Text> locationTable = new HashMap<Integer, ASTNode.Text>();

   /** The current line number. */
   private int lineNumber = 1;

   public GroovyTemplateBuilder(String templateId)
   {
      this.templateId = templateId;
   }

   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();

      // Add main stuff
      builder.append(out.toString());

      //
      builder.append("\n");
      builder.append("public static class Constants\n");
      builder.append("{\n");

      // Add text constant
      for (TextConstant method : textMethods)
      {
         builder.append(method.getDeclaration()).append("\n");
      }

      // Add line table
      builder.append("public static final Map<Integer, ").append(ASTNode.Text.class.getName()).append("> TABLE = ");
      if (locationTable.isEmpty())
      {
         builder.append("[:]");
      }
      else
      {
         builder.append("[\n");
         for (Iterator<Map.Entry<Integer, ASTNode.Text>> i = locationTable.entrySet().iterator();i.hasNext();)
         {
            Map.Entry<Integer, ASTNode.Text> entry = i.next();
            ASTNode.Text text = entry.getValue();
            Location location = text.getPosition();
            builder.append(entry.getKey()).append(':').
               append("new ").append(ASTNode.Text.class.getName()).append("(").
               append("new ").append(Location.class.getName()).append("(").append(location.getCol()).append(',').append(location.getLine()).append("),").
               append("'");
            Tools.escape(text.getData(), builder);
            builder.append("')");
            if (i.hasNext())
            {
               builder.append(",\n");
            }
            else
            {
               builder.append(']');
            }
         }
      }
      builder.append(";\n");

      // Close context
      builder.append("}\n");

      //
      return builder.toString();
   }

   @Override
   public GroovyTemplate build()
   {
      final String script = toString();
      return new GroovyTemplate(templateId)
      {
         @Override
         public String getScript()
         {
            return script;
         }
      };
   }

   public void startScriptlet()
   {
   }

   public void appendScriptlet(ASTNode.Text scriptlet)
   {
      out.append(scriptlet.getData());
      locationTable.put(lineNumber, scriptlet);
   }

   public void endScriptlet()
   {
      // We append a line break because we want that any line comment does not affect the template
      out.append("\n");
      lineNumber++;
   }

   public void startExpression()
   {
      out.append(";out.print(\"${");
   }

   public void appendExpression(ASTNode.Text expr)
   {
      out.append(expr.getData());
      locationTable.put(lineNumber, expr);
   }

   public void endExpression()
   {
      out.append("}\");\n");
      lineNumber++;
   }

   public void appendText(String text)
   {
      TextConstant m = new TextConstant("s" + methodCount++, text);
      out.append("out.print(Constants.").append(m.name).append(");\n");
      textMethods.add(m);
      lineNumber++;
   }

   public void appendLineBreak(SectionType currentType)
   {
      switch (currentType)
      {
         case SCRIPTLET:
            out.append("\n");
            lineNumber++;
            break;
         case EXPR:
            out.append("\n");
            lineNumber++;
            break;
         default:
            throw new AssertionError();
      }
   }
}
