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
import org.juzu.impl.spi.template.gtmpl.GroovyTemplate;
import org.juzu.impl.spi.template.gtmpl.GroovyTemplateGenerator;
import org.juzu.template.TemplateRenderContext;
import org.juzu.text.WriterPrinter;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Random;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateBuilderTestCase extends TestCase
{

   public void testFoo() throws Exception
   {
      GroovyTemplateGenerator template = new GroovyTemplateGenerator();
      ASTNode.Template.parse("a<%=foo%>c").generate(template, new TemplateCompilationContext());
      GroovyTemplate s = template.build("template_" + Math.abs(new Random().nextLong()));
      StringWriter out = new StringWriter();
      s.render(new TemplateRenderContext(new WriterPrinter(out), Collections.singletonMap("foo", "b")));
      assertEquals("abc", out.toString());
   }

}
