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

import org.juzu.impl.spi.inject.InjectImplementation;
import org.juzu.impl.spi.template.gtmpl.GroovyTemplateEmitter;
import org.juzu.impl.template.ast.ASTNode;
import org.juzu.impl.template.compiler.EmitContext;
import org.juzu.impl.template.compiler.EmitPhase;
import org.juzu.impl.template.compiler.ProcessContext;
import org.juzu.impl.template.compiler.ProcessPhase;
import org.juzu.impl.template.compiler.Template;
import org.juzu.impl.template.metadata.TemplateDescriptor;
import org.juzu.impl.utils.FQN;
import org.juzu.test.AbstractInjectTestCase;
import org.juzu.test.protocol.mock.MockApplication;
import org.juzu.test.protocol.mock.MockClient;
import org.juzu.test.protocol.mock.MockRenderBridge;

import java.util.HashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TagTestCase extends AbstractInjectTestCase
{

   public void _testSimple() throws Exception
   {
      MockApplication<?> app = application("template", "tag", "simple").init();
      app.init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      String out = render.assertStringResult();
      assertEquals("<foo>bar</foo>", out);
   }

   public void testDecorate() throws Exception
   {
      MockApplication<?> app = application("template", "tag", "decorate").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      String out = render.assertStringResult();
      assertEquals("<foo>bar</foo>", out);
   }

   public void testInclude() throws Exception
   {
      MockApplication<?> app = application("template", "tag", "include").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      String out = render.assertStringResult();
      assertEquals("foo", out);
   }

   public void testTitle() throws Exception
   {
      MockApplication<?> app = application("template", "tag", "title").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      String url = render.assertStringResult();
      assertEquals("the_title", render.getTitle());
      render = (MockRenderBridge)client.invoke(url);
      assertEquals("4", render.getTitle());
   }

   public void testParam() throws Exception
   {
      if (getDI() != InjectImplementation.INJECT_GUICE)
      {
         MockApplication<?> app = application("template", "tag", "param").init();

         //
         MockClient client = app.client();
         MockRenderBridge render = client.render();
         String content = render.assertStringResult();
         assertEquals("foo_value", content);
      }
   }
   
   public void testRecompileTemplate() throws Exception
   {
      MockApplication<?> app = application("template", "tag", "decorate").init();
      
      // Manufacture a template
      // to be removed later when we improve this
      // but for now it will be enough
      TemplateDescriptor desc = app.getContext().getDescriptor().getTemplates().getTemplate("foo.gtmpl");
      assertNotNull(desc);
      Template foo = new Template("index.gtmpl", new ASTNode.Template(), new FQN(desc.getType()), "groovy", "foo.gtmpl", System.currentTimeMillis());

      //
      HashMap<String, Template> templates = new HashMap<String, Template>();
      templates.put("foo.gtmpl", foo);
      ProcessPhase process = new ProcessPhase(new ProcessContext()
      {
         @Override
         protected Template resolveTemplate(String originPath, String path)
         {
            if (path.equals("index.gtmpl"))
            {
               try
               {
                  return new Template(
                     "index.gtmpl",
                     ASTNode.Template.parse("#{decorate path=foo.gtmpl/}juu"),
                     new FQN("template.tag.decorate.templates.index"),
                     "gtmpl",
                     "index.gtmpl",
                     System.currentTimeMillis()
                  );
               }
               catch (org.juzu.impl.template.ast.ParseException e)
               {
                  throw failure(e);
               }
            }
            else
            {
               return null;
            }
         }
      }, templates);
      Template template = process.resolveTemplate("index.gtmpl");
      assertNotNull(template);
      
      // Now emit the template
      EmitPhase emit = new EmitPhase(new EmitContext());
      emit.emit(new GroovyTemplateEmitter(), template.getAST());
   }
}
