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

package org.juzu.impl.spi.template.gtmpl;

import org.juzu.impl.spi.template.MethodInvocation;
import org.juzu.impl.spi.template.TemplateGenerator;
import org.juzu.impl.spi.template.TemplateGeneratorContext;
import org.juzu.impl.template.SectionType;
import org.juzu.impl.utils.Tools;
import org.juzu.utils.Location;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class GroovyTemplateGenerator extends TemplateGenerator
{

   /** . */
   private StringBuilder out = new StringBuilder();

   /** . */
   private List<TextConstant> textMethods = new ArrayList<TextConstant>();

   /** . */
   private int methodCount = 0;

   /** The line number table. */
   private HashMap<Integer, Foo> locationTable = new HashMap<Integer, Foo>();

   /** The current line number. */
   private int lineNumber = 1;

   /** . */
   private Location pos;

   /** . */
   private final TemplateGeneratorContext context;

   public GroovyTemplateGenerator(TemplateGeneratorContext context)
   {
      this.pos = null;
      this.context = context;
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
      builder.append("public static final Map<Integer, ").append(Foo.class.getName()).append("> TABLE = ");
      if (locationTable.isEmpty())
      {
         builder.append("[:]");
      }
      else
      {
         builder.append("[\n");
         for (Iterator<Map.Entry<Integer, Foo>> i = locationTable.entrySet().iterator();i.hasNext();)
         {
            Map.Entry<Integer, Foo> entry = i.next();
            Foo text = entry.getValue();
            Location location = text.getPosition();
            builder.append(entry.getKey()).append(':').
               append("new ").append(Foo.class.getName()).append("(").
               append("new ").append(Location.class.getName()).append("(").append(location.getCol()).append(',').append(location.getLine()).append("),").
               append("'");
            Tools.escape(text.getValue(), builder);
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

   public GroovyTemplate build(String templateId)
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

   public void startScriptlet(Location beginPosition)
   {
      pos = beginPosition;
   }

   public void appendScriptlet(String scriptlet)
   {
      out.append(scriptlet);
      locationTable.put(lineNumber, new Foo(pos, scriptlet));
   }

   public void endScriptlet()
   {
      // We append a line break because we want that any line comment does not affect the template
      out.append("\n");
      lineNumber++;
   }

   public void startExpression(Location beginPosition)
   {
      pos = beginPosition;
      out.append(";out.print(\"${");
   }

   public void appendExpression(String expr)
   {
      out.append(expr);
      locationTable.put(lineNumber, new Foo(pos, expr));
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

   public void appendLineBreak(SectionType currentType, Location position)
   {
      this.pos = new Location(1, position.getLine() + 1);
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

   @Override
   public void url(String typeName, String methodName, Map<String, String> args)
   {
      if (context != null)
      {
         MethodInvocation mi = context.resolveMethodInvocation(typeName, methodName, args);
         out.append(";out.print(");
         out.append(mi.getClassName());
         out.append(".");
         out.append(mi.getMethodName());
         out.append("(");
         for (int i = 0;i < mi.getMethodArguments().size();i++)
         {
            if (i > 0)
            {
               out.append(",");
            }
            String methodArg = mi.getMethodArguments().get(i);
            out.append(methodArg);
         }
         out.append("));");
      }
   }

   public void generate(Filer filer, String pkgName, String rawName) throws IOException
   {
      String script = toString();

      // Create the groovy file
      FileObject fo = filer.createResource(StandardLocation.CLASS_OUTPUT, pkgName, rawName + ".groovy");
      Writer writer = fo.openWriter();
      try
      {
         writer.write(script);
      }
      finally
      {
         Tools.safeClose(writer);
      }

      // Create the class associated with the template
      String fqn = pkgName.length() == 0 ? rawName : (pkgName + "." + rawName);
      FileObject fof = filer.createSourceFile(fqn);
      writer = fof.openWriter();
      try
      {
         writer.append("package ").append(pkgName).append(";\n");
         writer.append("public class ").append(rawName).append(" extends ").append(GroovyTemplateLiteral.class.getName()).append("\n");
         writer.append("{\n");
         writer.append("public ").append(rawName).append("()\n");
         writer.append("{\n");
         writer.append("}\n");
         writer.append("}\n");
      }
      finally
      {
         writer.close();
      }
   }
}
