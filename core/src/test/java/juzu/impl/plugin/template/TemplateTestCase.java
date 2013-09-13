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

import juzu.impl.common.Content;
import juzu.impl.compiler.CompilationError;
import juzu.impl.compiler.Compiler;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.ram.RAMFileSystem;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.plugin.template.metamodel.TemplateMetaModel;
import juzu.test.AbstractInjectTestCase;
import juzu.test.CompilerAssert;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateTestCase extends AbstractInjectTestCase {

  public TemplateTestCase(InjectorProvider di) {
    super(di);
  }

  public void _testSimple() throws Exception {
    CompilerAssert<?, ?> helper = compiler("plugin.template.simple");
    Compiler compiler = helper.assertCompile();

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

  @Test
  public void testControllerNotFound() throws Exception {
    CompilerAssert<File, File> compiler = compiler("plugin.template.controllerNotFound");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(TemplateMetaModel.CONTROLLER_NOT_RESOLVED, error.getCode());
    assertEquals(Arrays.asList("Foo.bar({})", "index.gtmpl", "2", "4"), error.getArguments());
  }

  @Test
  public void testRelativePath() throws Exception {
    MockApplication<?> app = application("plugin.template.relativepath").init();
    MockClient client = app.client();
    assertEquals("relative_path_template", client.render().assertStringResult());
  }

  @Test
  public void testTyped() throws Exception {
    MockApplication<?> app = application("plugin.template.typed").init();
    MockClient client = app.client();
    assertEquals("typed_template", client.render().assertStringResult());
  }

  @Test
  public void testUndeclaredIOE() throws Exception {
    MockApplication<?> app = application("plugin.template.ioe").init();
    MockClient client = app.client();
    assertEquals("pass", client.render().assertStringResult());
  }

  @Test
  public void testSyntaxError() throws Exception {
    MockApplication<?> app = application("plugin.template.syntaxerror").init();
  }

  @Test
  public void testMessage() throws Exception {
    MockApplication<?> app = application("plugin.template.message").init();
    app.addMessage(Locale.ENGLISH, "the_key", "the_key_en");
    MockClient client = app.client();
    assertEquals("(the_key_en,)", client.render().assertStringResult());
  }

  @Test
  public void testPrecompileGroovy() throws Exception {
    MockApplication<File> app = application("plugin.template.simple").init();
    ReadFileSystem<File> fs = app.getClasses();
    File groovy = fs.getPath("plugin", "template", "simple", "templates", "index_.groovy");
    assertNotNull(groovy);
    CompilerConfiguration config = new CompilerConfiguration();
    config.setTargetDirectory(fs.getRoot());
    CompilationUnit cu = new CompilationUnit(config);
    cu.addSource(groovy);
    cu.compile();
    assertTrue(groovy.delete());
    MockClient client = app.client();
    assertEquals("hello", client.render().assertStringResult());
  }

  @Test
  public void testAliasesFromSourcepath() throws Exception {
    MockApplication<?> app = application("plugin.template.aliases.sourcepath").init();
    MockClient client = app.client();
    assertEquals("sourcepath_alias", client.render().assertStringResult());
  }

  @Test
  public void testAliasesFromClassPath() throws Exception {
    CompilerAssert<File, File> helper = compiler("plugin.template.aliases.classpath");
    RAMFileSystem classpath = new RAMFileSystem();
    classpath.setContent(new String[]{"bar.gtmpl"}, new Content("the template"));
    helper.addClassPath(classpath);
    helper.assertCompile();
  }
}
