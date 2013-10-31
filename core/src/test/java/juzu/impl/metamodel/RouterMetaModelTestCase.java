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

package juzu.impl.metamodel;

import juzu.impl.common.Name;
import juzu.impl.common.Tools;
import juzu.impl.compiler.CompilationError;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.plugin.router.ParamDescriptor;
import juzu.impl.plugin.router.metamodel.RouteMetaModel;
import juzu.impl.plugin.router.metamodel.RouterMetaModel;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouterMetaModelTestCase extends AbstractTestCase {

  @Test
  public void testDuplicateMethodRoute() throws Exception {
    CompilerAssert<File, File> helper = compiler("metamodel.router.duplicate.methodroute").formalErrorReporting(true);
    List<CompilationError> errors =  helper.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertSame(RouterMetaModel.ROUTER_DUPLICATE_ROUTE, error.getCode());
    assertEquals(Arrays.asList("/foo"), error.getArguments());
    File f = helper.getSourcePath().getPath("metamodel", "router", "duplicate", "methodroute", "A.java");
    assertEquals(f, error.getSourceFile());
  }

  @Test
  public void testParam() throws Exception {
    CompilerAssert<File, File> helper = compiler("metamodel.router.param").formalErrorReporting(true);
    helper.assertCompile();
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    ModuleMetaModel mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;
    ApplicationMetaModel application = mm.getChild(Key.of(ElementHandle.Package.create(Name.parse("metamodel.router.param")), ApplicationMetaModel.class));
    RouterMetaModel router = application.getChild(Key.of(RouterMetaModel.class));
    RouteMetaModel root = router.getRoot();
    RouteMetaModel route = root.getChildren().get(0);
    assertEquals("/{foo}", route.getPath());
    assertEquals(Collections.singleton("foo"), route.getParameters().keySet());
    ParamDescriptor foo =  route.getParameters().get("foo");
    assertEquals(".*", foo.getPattern());
    assertEquals(Boolean.TRUE, foo.getPreservePath());
    assertEquals(Boolean.FALSE, foo.getCaptureGroup());
  }
}
