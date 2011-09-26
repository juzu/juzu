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

import junit.framework.TestCase;
import org.juzu.impl.template.groovy.GroovyTemplate;
import org.juzu.impl.template.groovy.GroovyTemplateBuilder;
import org.juzu.template.TemplateExecutionException;
import org.juzu.text.WriterPrinter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractTemplateTestCase extends TestCase
{

   public GroovyTemplate template(String text)
   {
      TemplateParser parser = new TemplateParser();
      GroovyTemplateBuilder templateWriter = new GroovyTemplateBuilder("template_" + Math.abs(new Random().nextLong()));
      parser.parse(text).build(templateWriter);
      return templateWriter.build();
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
