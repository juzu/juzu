/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package juzu.impl.plugin.template;

import juzu.impl.inject.spi.InjectImplementation;
import juzu.impl.template.spi.EmitContext;
import juzu.impl.template.spi.juzu.dialect.gtmpl.GroovyTemplateEmitter;
import juzu.impl.template.spi.juzu.ast.ASTNode;
import juzu.impl.template.spi.juzu.compiler.EmitPhase;
import juzu.impl.template.spi.ProcessContext;
import juzu.impl.template.spi.juzu.compiler.ProcessPhase;
import juzu.impl.template.spi.Template;
import juzu.impl.plugin.template.metadata.TemplateDescriptor;
import juzu.impl.common.Path;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockRenderBridge;
import org.junit.Test;

import java.io.Serializable;
import java.util.HashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TagTestCase extends AbstractInjectTestCase {

  public TagTestCase(InjectImplementation di) {
    super(di);
  }

  public void _testSimple() throws Exception {
    MockApplication<?> app = application("plugin", "template", "tag", "simple").init();
    app.init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    String out = render.assertStringResult();
    assertEquals("<foo>bar</foo>", out);
  }

  @Test
  public void testDecorate() throws Exception {
    MockApplication<?> app = application("plugin", "template", "tag", "decorate").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    String out = render.assertStringResult();
    assertEquals("<foo>bar</foo>", out);
  }

  @Test
  public void testInclude() throws Exception {
    MockApplication<?> app = application("plugin", "template", "tag", "include").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    String out = render.assertStringResult();
    assertEquals("foo", out);
  }

  @Test
  public void testTitle() throws Exception {
    MockApplication<?> app = application("plugin", "template", "tag", "title").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    String url = render.assertStringResult();
    assertEquals("the_title", render.getTitle());
    render = (MockRenderBridge)client.invoke(url);
    assertEquals("4", render.getTitle());
  }

  @Test
  public void testParam() throws Exception {
    if (getDI() != InjectImplementation.INJECT_GUICE) {
      MockApplication<?> app = application("plugin", "template", "tag", "param").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      String content = render.assertStringResult();
      assertEquals("foo_value", content);
    }
  }

  @Test
  public void testRecompileTemplate() throws Exception {
    MockApplication<?> app = application("plugin", "template", "tag", "decorate").init();

    // Manufacture a template
    // to be removed later when we improve this
    // but for now it will be enough
    TemplateDescriptor desc = app.getContext().getDescriptor().getTemplates().getTemplate("foo.gtmpl");
    assertNotNull(desc);
    Template<?> foo = new Template<ASTNode.Template>(
      Path.parse("index.gtmpl"),
      new ASTNode.Template(),
      Path.parse(desc.getType().getName().replace('.', '/') + "/foo.gtmpl"),
      System.currentTimeMillis());

    //
    HashMap<Path, Template<?>> templates = new HashMap<Path, Template<?>>();
    templates.put(Path.parse("foo.gtmpl"), foo);
    ProcessPhase process = new ProcessPhase(new ProcessContext(templates) {
      @Override
      public <A extends Serializable> Template<A> resolveTemplate(Path originPath, Path path) {
        if (path.equals(Path.parse("index.gtmpl"))) {
          try {
            return (Template<A>)new Template<ASTNode.Template>(
              Path.parse("index.gtmpl"),
              ASTNode.Template.parse("#{decorate path=foo.gtmpl/}juu"),
              Path.parse("plugin/template/tag/decorate/templates/index.gtmpl"),
              System.currentTimeMillis()
            );
          }
          catch (juzu.impl.template.spi.juzu.ast.ParseException e) {
            throw failure(e);
          }
        }
        else {
          return null;
        }
      }
    });
    Template<ASTNode.Template> template = (Template<ASTNode.Template>)process.resolveTemplate(Path.parse("index.gtmpl"));
    assertNotNull(template);

    // Now emit the template
    EmitPhase emit = new EmitPhase(new EmitContext());
    emit.emit(new GroovyTemplateEmitter(), template.getModel());
  }
}
