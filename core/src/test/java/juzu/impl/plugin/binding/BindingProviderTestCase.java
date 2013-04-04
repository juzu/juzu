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

package juzu.impl.plugin.binding;

import juzu.impl.compiler.CompilationError;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.test.AbstractInjectTestCase;
import juzu.test.CompilerAssert;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockRenderBridge;
import org.junit.Test;

import java.io.File;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BindingProviderTestCase extends AbstractInjectTestCase {

  public BindingProviderTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testProvider() throws Exception {
    MockApplication<?> app = application("plugin.binding.provider.create").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("pass", render.assertStringResult());
  }

  @Test
  public void testNotAssignable() throws Exception {
    CompilerAssert<File, File> compiler = compiler("plugin.binding.provider.notassignable");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(BindingMetaModelPlugin.PROVIDER_NOT_ASSIGNABLE, error.getCode());
    File f = compiler.getSourcePath().getPath("plugin", "binding", "provider", "notassignable", "package-info.java");
    assertEquals(f, error.getSourceFile());
  }

  @Test
  public void testAbstractClass() throws Exception {
    CompilerAssert<File, File> compiler = compiler("plugin.binding.provider.abstractclass");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(BindingMetaModelPlugin.IMPLEMENTATION_NOT_ABSTRACT, error.getCode());
    File f = compiler.getSourcePath().getPath("plugin", "binding", "provider", "abstractclass", "package-info.java");
    assertEquals(f, error.getSourceFile());
  }

  @Test
  public void testQualifiedProvider() throws Exception {
    MockApplication<?> app = application("plugin.binding.provider.qualified").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("pass", render.assertStringResult());
  }
}
