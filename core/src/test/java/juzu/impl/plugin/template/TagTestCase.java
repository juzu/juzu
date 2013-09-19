/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.plugin.template;

import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.template.spi.EmitContext;
import juzu.impl.template.spi.SimpleProcessContext;
import juzu.impl.template.spi.juzu.dialect.gtmpl.GroovyTemplateEmitter;
import juzu.impl.template.spi.juzu.ast.ASTNode;
import juzu.impl.template.spi.juzu.compiler.EmitPhase;
import juzu.impl.template.spi.juzu.compiler.ProcessPhase;
import juzu.impl.template.spi.Template;
import juzu.impl.plugin.template.metadata.TemplateDescriptor;
import juzu.impl.common.Path;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockRenderBridge;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TagTestCase extends AbstractInjectTestCase {

  public TagTestCase(InjectorProvider di) {
    super(di);
  }

  public void _testSimple() throws Exception {
    MockApplication<?> app = application("plugin.template.tag.simple").init();
    app.init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    String out = render.assertStringResult();
    assertEquals("<foo>bar</foo>", out);
  }

  @Test
  public void testDecorate() throws Exception {
    MockApplication<?> app = application("plugin.template.tag.decorate").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    String out = render.assertStringResult();
    assertEquals("<foo>bar</foo>", out);
  }

  @Test
  public void testDecorateNested() throws Exception {
    MockApplication<?> app = application("plugin.template.tag.decoratenested").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    String out = render.assertStringResult();
    assertEquals("<juu><foo>bar</foo></juu>", out);
  }

  @Test
  public void testInclude() throws Exception {
    MockApplication<?> app = application("plugin.template.tag.include").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    String out = render.assertStringResult();
    assertEquals("foo", out);
  }

  @Test
  public void testTitle() throws Exception {
    MockApplication<?> app = application("plugin.template.tag.title").init();

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
    if (getDI() != InjectorProvider.INJECT_GUICE) {
      MockApplication<?> app = application("plugin.template.tag.param").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      String content = render.assertStringResult();
      assertEquals("foo_value", content);
    }
  }

  @Test
  public void testRecompileTemplate() throws Exception {
    MockApplication<?> app = application("plugin.template.tag.decorate").init();

    // Manufacture a template
    // to be removed later when we improve this
    // but for now it will be enough
    TemplateDescriptor desc = app.getLifeCycle().resolveBean(TemplatePlugin.class).getDescriptor().getTemplate("foo.gtmpl");
    assertNotNull(desc);
    Template<?> foo = new Template<ASTNode.Template>(
      new ASTNode.Template(),
      (Path.Relative)Path.parse(desc.getType().getName().replace('.', '/') + "/foo.gtmpl"),
      (Path.Absolute)Path.parse("/" + desc.getType().getName().replace('.', '/') + "/foo.gtmpl"),
      System.currentTimeMillis());

    //
    HashMap<Path, Template<?>> templates = new HashMap<Path, Template<?>>();
    templates.put(Path.parse("foo.gtmpl"), foo);
    ProcessPhase process = new ProcessPhase(new SimpleProcessContext(templates) {
      @Override
      public <A extends Serializable> Template<A> resolveTemplate(Path.Relative originPath, Path.Relative path) {
        if (path.getCanonical().equals("index.gtmpl")) {
          try {
            return (Template<A>)new Template<ASTNode.Template>(
              ASTNode.Template.parse("#{decorate path=foo.gtmpl/}juu"),
              (Path.Relative)Path.parse("plugin/template/tag/decorate/templates/index.gtmpl"),
              (Path.Absolute)Path.parse("/plugin/template/tag/decorate/templates/index.gtmpl"),
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
    Template<ASTNode.Template> template = (Template<ASTNode.Template>)process.resolveTemplate((Path.Relative)Path.parse("index.gtmpl"));
    assertNotNull(template);

    // Now emit the template
    EmitPhase emit = new EmitPhase(new EmitContext(){
      public void createResource(String rawName, String ext, CharSequence content) throws IOException {
        throw new UnsupportedOperationException();
      }
    });
    emit.emit(new GroovyTemplateEmitter(), template.getModel());
  }
}
