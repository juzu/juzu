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

package juzu.impl.metamodel;

import juzu.impl.common.QN;
import juzu.impl.common.Tools;
import juzu.impl.compiler.CompilationError;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.plugin.router.metamodel.RouteMetaModel;
import juzu.impl.plugin.router.metamodel.RouterMetaModel;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouterTestCase extends AbstractTestCase {

  @Test
  public void testModule() throws Exception {
    CompilerAssert<File, File> helper = compiler("metamodel", "router", "module");
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    ModuleMetaModel mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

    //
    RouterMetaModel router = mm.getChild(RouterMetaModel.KEY);
    assertNotNull(router);
    RouteMetaModel root = router.getRoot();
    assertNotNull(root);
    assertEquals(null, root.getTargets());
    assertEquals(Arrays.asList(0), root.getPriorities());
    Map<String, RouteMetaModel> children = root.getChildren(0);
    assertEquals(2, children.size());
    assertEquals(Tools.set("/app1", "/app2"), children.keySet());
    RouteMetaModel app1 = children.get("/app1");
    assertNull(app1.getPriorities());
    assertEquals(Collections.singletonMap("application", "metamodel.router.module.app1"), app1.getTargets());
    RouteMetaModel app2 = children.get("/app2");
    assertNull(app2.getPriorities());
    assertEquals(Collections.singletonMap("application", "metamodel.router.module.app2"), app2.getTargets());
  }

  @Test
  public void testDuplicateMethodRoute() throws Exception {
    CompilerAssert<File, File> helper = compiler("metamodel", "router", "duplicate", "methodroute").formalErrorReporting(true);
    List<CompilationError> errors =  helper.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertSame(RouterMetaModel.ROUTER_DUPLICATE_ROUTE, error.getCode());
    assertEquals(Arrays.asList("/foo"), error.getArguments());
    assertEquals("/metamodel/router/duplicate/methodroute/A.java", error.getSource());
  }

  @Test
  public void testDuplicatePackageRoute() throws Exception {
    CompilerAssert<File, File> helper = compiler("metamodel", "router", "duplicate", "packageroute").formalErrorReporting(true);
    List<CompilationError> errors =  helper.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertSame(RouterMetaModel.ROUTER_DUPLICATE_ROUTE, error.getCode());
    assertEquals(Arrays.asList("/foo"), error.getArguments());
    String src = error.getSource();
    assertTrue(Pattern.compile("/metamodel/router/duplicate/packageroute/app[12]/package-info\\.java").matcher(src).matches());
  }

  @Test
  public void testParamPattern() throws Exception {
    CompilerAssert<File, File> helper = compiler("metamodel", "router", "param", "pattern").formalErrorReporting(true);
    helper.assertCompile();
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    ModuleMetaModel mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;
    ApplicationMetaModel application = mm.getChild(Key.of(ElementHandle.Package.create(QN.create("metamodel", "router", "param", "pattern")), ApplicationMetaModel.class));
    RouterMetaModel router = application.getChild(Key.of(RouterMetaModel.class));
    RouteMetaModel root = router.getRoot();
    RouteMetaModel route = root.getChildren(0).get("/{foo}");
    assertEquals(Collections.singletonMap("foo", ".*"), route.getParameters());
  }
}
