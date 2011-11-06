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

import org.juzu.impl.compiler.Compiler;
import org.juzu.test.AbstractInjectTestCase;
import org.juzu.test.CompilerHelper;
import org.juzu.test.request.MockApplication;
import org.juzu.test.request.MockClient;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateTestCase extends AbstractInjectTestCase
{

   public void _testSimple() throws Exception
   {
      CompilerHelper<?, ?> helper = compiler("template", "simple");
      Compiler<?, ?> compiler = helper.assertCompile();

      //
/*
      Content content = compiler.getClassOutput(FileKey.newResourceName("template.simple.templates", "index.groovy"));
      assertNotNull(content);
      assertTrue(compiler.getClassOutputKeys().size() > 0);

      //
      assertTrue(compiler.getSourceOutputKeys().size() > 1);
      Content content2 = compiler.getSourceOutput(FileKey.newJavaName("template.simple.templates.index", JavaFileObject.Kind.SOURCE));
      assertNotNull(content2);

      //
      ClassLoader cl = new URLClassLoader(new URL[]{helper.getOutput().getURL()}, Thread.currentThread().getContextClassLoader());

      //
      Class<?> aClass = cl.loadClass("template.simple.A");
      Class<?> bClass = cl.loadClass("template.simple.templates.index");
      TemplateStub template = (TemplateStub)bClass.newInstance();
      StringWriter out = new StringWriter();
      template.render(new TemplateRenderContext(new WriterPrinter(out)));
      assertEquals("hello", out.toString());
*/
   }

   public void testRelativePath() throws Exception
   {
      MockApplication<?> app = application("template", "relativepath").init();
      MockClient client = app.client();
      assertEquals("relative_path_template", client.render().getContent());

   }
}
