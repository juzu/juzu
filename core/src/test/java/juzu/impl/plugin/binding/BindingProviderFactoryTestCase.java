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

import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.compiler.CompilationError;
import juzu.inject.ProviderFactory;
import juzu.test.AbstractInjectTestCase;
import juzu.test.CompilerAssert;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockRenderBridge;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BindingProviderFactoryTestCase extends AbstractInjectTestCase {

  public BindingProviderFactoryTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testNoPublicCtor() throws Exception {
    CompilerAssert<File, File> compiler = compiler("plugin.binding.provider.factory.nopublicctor");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(BindingMetaModelPlugin.PROVIDER_FACTORY_NO_PUBLIC_CTOR, error.getCode());
    File f = compiler.getSourcePath().getPath("plugin", "binding", "provider", "factory", "nopublicctor", "package-info.java");
    assertEquals(f, error.getSourceFile());
  }

  @Test
  public void testNoZeroCtor() throws Exception {
    CompilerAssert<File, File> compiler = compiler("plugin.binding.provider.factory.nozeroargctor");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(BindingMetaModelPlugin.PROVIDER_FACTORY_NO_ZERO_ARG_CTOR, error.getCode());
    File f = compiler.getSourcePath().getPath("plugin", "binding", "provider", "factory", "nozeroargctor", "package-info.java");
    assertEquals(f, error.getSourceFile());
  }

  @Test
  public void testAbstractClass() throws Exception {
    CompilerAssert<File, File> compiler = compiler("plugin.binding.provider.factory.abstractclass");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(BindingMetaModelPlugin.IMPLEMENTATION_NOT_ABSTRACT, error.getCode());
    File f = compiler.getSourcePath().getPath("plugin", "binding", "provider", "factory", "abstractclass", "package-info.java");
    assertEquals(f, error.getSourceFile());
  }

  @Test
  public void testNotPublicClass() throws Exception {
    CompilerAssert<File, File> compiler = compiler("plugin.binding.provider.factory.notpublicclass");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(BindingMetaModelPlugin.PROVIDER_FACTORY_NOT_PUBLIC, error.getCode());
    File f = compiler.getSourcePath().getPath("plugin", "binding", "provider", "factory", "notpublicclass", "package-info.java");
    assertEquals(f, error.getSourceFile());
  }

  @Test
  public void testCreate() throws Exception {
    MockApplication<File> app = application("plugin.binding.provider.factory.create");
    File root = app.getClasses().getRoot();
    File services = new File(root, "META-INF/services");
    assertTrue(services.mkdirs());
    File providers = new File(services, ProviderFactory.class.getName());
    FileWriter writer = new FileWriter(providers);
    writer.append("plugin.binding.provider.factory.create.ProviderFactoryImpl");
    writer.close();
    app.init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("pass", render.assertStringResult());
  }

  @Test
  public void testGetProviderThrowable() throws Exception {
    MockApplication<File> app = application("plugin.binding.provider.factory.throwable");
    File root = app.getClasses().getRoot();
    File services = new File(root, "META-INF/services");
    assertTrue(services.mkdirs());
    File providers = new File(services, ProviderFactory.class.getName());
    FileWriter writer = new FileWriter(providers);
    writer.append("plugin.binding.provider.factory.throwable.ServiceProviderFactory");
    writer.close();

    //
    try {
      app.init();
      fail();
    }
    catch (SecurityException e) {
    }
  }
}
