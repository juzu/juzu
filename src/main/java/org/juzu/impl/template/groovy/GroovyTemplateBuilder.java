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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
      builder.append(out.toString());
      builder.append("\n");
      builder.append("public class Constants\n");
      builder.append("{\n");
      for (TextConstant method : textMethods)
      {
         builder.append(method.getDeclaration()).append("\n");
      }
      builder.append("}\n");
      return builder.toString();
   }

   @Override
   public GroovyTemplate build()
   {
      return new GroovyTemplate(templateId, toString(), locationTable);
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

      //   public String build()
      //   {

            //
      /*
            CompilerConfiguration config = new CompilerConfiguration();

            //
            byte[] bytes;
            try
            {
               config.setScriptBaseClass(BaseScript.class.getName());
               bytes = groovyText.getBytes(config.getSourceEncoding());
            }
            catch (UnsupportedEncodingException e)
            {
               throw new TemplateCompilationException(e, groovyText);
            }

            //
            InputStream in = new ByteArrayInputStream(bytes);
            GroovyCodeSource gcs = new GroovyCodeSource(in, templateName, "/groovy/shell");
            GroovyClassLoader loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
            Class<?> scriptClass;
            try
            {
               scriptClass = loader.parseClass(gcs, false);
            }
            catch (CompilationFailedException e)
            {
               throw new GroovyCompilationException(e, templateText, groovyText);
            }
            catch (ClassFormatError e)
            {
               throw new GroovyCompilationException(e, templateText, groovyText);
            }

            return new GroovyScript(
               templateId,
               script.toString(),
               scriptClass,
               Collections.unmodifiableMap(new HashMap<Integer, TextItem>(script.positionTable))
            );
      */
      //   }
}
