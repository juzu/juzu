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
package examples.plugin.bundlegen;

import examples.plugin.bundlegen.impl.BundleGenMetaModelPlugin;
import juzu.impl.compiler.CompilationError;
import juzu.impl.fs.spi.ReadWriteFileSystem;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockViewBridge;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;

/**
 * @author Julien Viet
 */
public class BundleGenTestCase extends AbstractTestCase {

  // tag::testGenerateBundle[]
  @Test
  public void testGenerateBundle() throws Exception {
    CompilerAssert<File, File> compilerAssert = compiler("examples.app1"); //<1>
    compilerAssert.assertCompile();                                        //<2>
    ReadWriteFileSystem<File> output = compilerAssert.getClassOutput();    //<3>
    ClassLoader loader = new URLClassLoader(new URL[]{ output.getURL() }); //<4>
    Class<?> bundleClass = loader.loadClass("examples.app1.mybundle");     //<5>
    Method m = bundleClass.getDeclaredMethod("abc");                       //<6>
    assertEquals(String.class, m.getReturnType());
  }
  // end::testGenerateBundle[]

  // tag::testResolveBundle[]
  @Test
  public void testResolveBundle() throws Exception {
    MockApplication<File> application = application(InjectorProvider.GUICE, "examples.app1"); //<1>
    application.init();                                                                       //<2>
    MockClient client = application.client();
    MockViewBridge view = client.render();                                                    //<3>
    String markup = view.assertStringResponse();
    assertEquals("<abc>def</abc>", markup);                                                   //<4>
  }
  // end::testResolveBundle[]

  // tag::testBundleNotFound[]
  @Test
  public void testBundleNotFound() throws Exception {
    CompilerAssert<File, File> compilerAssert = compiler("examples.app2");
    compilerAssert.formalErrorReporting();                                     //<1>
    List<CompilationError> errors = compilerAssert.failCompile();              //<2>
    assertEquals(1, errors.size());                                            //<3>
    CompilationError error = errors.get(0);
    assertEquals(BundleGenMetaModelPlugin.BUNDLE_NOT_FOUND, error.getCode());  //<4>
    assertEquals(Collections.singletonList("mybundle"), error.getArguments());
    File source = error.getSourceFile();
    assertEquals("package-info.java", source.getName());
    assertEquals("app2", source.getParentFile().getName());
  }
  // end::testBundleNotFound[]
}
