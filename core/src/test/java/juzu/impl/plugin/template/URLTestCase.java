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

import juzu.impl.compiler.CompilationError;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockViewBridge;
import org.junit.Test;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class URLTestCase extends AbstractTestCase {

  @Test
  public void testResolution() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.template.url.resolution");
    compiler.assertCompile();
  }

//  @Test
  public void testInvalidMethodName() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.template.url.invalid_method_name");
    List<CompilationError> errors = compiler.failCompile();
    assertEquals("Was expecting 1 error instead of " + errors, 1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals("/plugin/template/url/invalid_method_name/A.java", error.getSource());
  }

//  @Test
  public void testInvalidMethodArgs() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.template.url.invalid_method_args");
    List<CompilationError> errors = compiler.failCompile();
    assertEquals("Was expecting 1 error instead of " + errors, 1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals("/plugin/template/url/invalid_method_args/A.java", error.getSource());
  }

//  @Test
  public void testOverload() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.template.url.overload");
    compiler.assertCompile();
  }

  @Test
  public void testContextutal() throws Exception {
    MockApplication<?> app = application(InjectorProvider.CDI_WELD, "plugin.template.url.contextual").init();
    MockClient client = app.client();
    String url = client.render().assertStringResult();
    assertEquals("pass", ((MockViewBridge)client.invoke(url)).assertStringResult());
  }
}
