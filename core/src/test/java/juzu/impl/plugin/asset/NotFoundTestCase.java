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
import juzu.impl.inject.spi.InjectorProvider;
import juzu.test.AbstractInjectTestCase;
import juzu.test.CompilerAssert;
import juzu.test.protocol.mock.MockApplication;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class NotFoundTestCase extends AbstractInjectTestCase {

  public NotFoundTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testCompile() throws Exception {
    CompilerAssert<?, ?> app  = compiler("plugin.asset.notfound.compile");
    app.formalErrorReporting();
    List<CompilationError> errors = app.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(AssetMetaModelPlugin.ASSET_NOT_FOUND, error.getCode());
  }

  @Test
  public void testRuntime() throws Exception {
    MockApplication<?> app  = application("plugin.asset.notfound.runtime");
    File root = (File)app.getSourcePath().getPath("plugin", "asset", "notfound", "runtime");
    File js = new File(root, "assets/notfound.js");
    assertTrue(js.getParentFile().mkdirs());
    assertTrue(js.createNewFile());
    app.assertCompile();
    root = (File)app.getClasses().getPath("plugin", "asset", "notfound", "runtime");
    js = new File(root, "assets/notfound.js");
    assertTrue(js.delete());
    try {
      app.init();
      fail();
    }
    catch (Exception ignore) {
    }
  }
}
