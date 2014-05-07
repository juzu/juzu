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

package juzu.impl.plugin.asset;

import juzu.impl.compiler.CompilationError;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MinifierFailureTestCase extends AbstractTestCase {

  @Test
  public void testCannotInstantiate() throws Exception {
    assertFail("plugin.asset.minifier.cannotinstantiate", "test-min.js", "java.lang.RuntimeException: Cannot instantiate");
  }

  @Test
  public void testCannotMinify() throws Exception {
    assertFail("plugin.asset.minifier.cannotminify", "test-min.js", "Cannot minify");
  }

  @Test
  public void testIOException() throws Exception {
    assertFail("plugin.asset.minifier.ioexception", "test-min.js", "Cannot read");
  }

  @Test
  public void testTypeGuard() throws Exception {
    assertFail("plugin.asset.minifier.typeguard", "test-min.js", "Can only process scripts and not stylesheet asset");
  }

  public static void assertFail(String name, String... expectedMessage) throws Exception {
    CompilerAssert<File, File> compiler = compiler(name);
    compiler.formalErrorReporting();
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    assertEquals(AssetMetaModelPlugin.CANNOT_PROCESS_ASSET, errors.get(0).getCode());
    assertEquals(Arrays.asList(expectedMessage), errors.get(0).getArguments());
  }
}
