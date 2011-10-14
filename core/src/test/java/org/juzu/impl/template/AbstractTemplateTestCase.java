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

import org.juzu.impl.spi.template.MethodInvocation;
import org.juzu.impl.spi.template.TemplateGeneratorContext;
import org.juzu.impl.spi.template.gtmpl.GroovyTemplate;
import org.juzu.impl.spi.template.gtmpl.GroovyTemplateGenerator;
import org.juzu.template.TemplateExecutionException;
import org.juzu.test.AbstractTestCase;
import org.juzu.text.WriterPrinter;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractTemplateTestCase extends AbstractTestCase
{

   public GroovyTemplate template(String text)
   {
      ASTBuilder parser = new ASTBuilder();
      GroovyTemplateGenerator templateWriter = new GroovyTemplateGenerator(new TemplateGeneratorContext()
      {
         public MethodInvocation resolveMethodInvocation(String typeName, String methodName, Map<String, String> parameterMap)
         {
            if (parameterMap.size() > 0)
            {
               throw failure("Unexpected non empty parameter map");
            }
            Class clazz = AbstractTemplateTestCase.this.getClass();
            try
            {
               Method m = clazz.getMethod(methodName);
               return new MethodInvocation(clazz.getName(), m.getName(), Collections.<String>emptyList());
            }
            catch (NoSuchMethodException e)
            {
               throw failure(e);
            }
         }
      });
      parser.parse(text).generate(templateWriter);
      return templateWriter.build("template_" + Math.abs(new Random().nextLong()));
   }

   public String render(String template) throws IOException, TemplateExecutionException
   {
      return render(template, null, null);
   }

   public String render(String template, Locale locale) throws IOException, TemplateExecutionException
   {
      return render(template, null, locale);
   }

   public String render(String text, Map<String, ?> binding, Locale locale) throws IOException, TemplateExecutionException
   {
      GroovyTemplate template = template(text);
      StringWriter out = new StringWriter();
      template.render(new WriterPrinter(out), binding, locale);
      return out.toString();
   }

   public String render(String template, Map<String, ?> binding) throws IOException, TemplateExecutionException
   {
      return render(template, binding, null);
   }
}
