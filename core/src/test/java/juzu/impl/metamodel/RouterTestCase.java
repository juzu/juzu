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
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouterTestCase extends AbstractTestCase {

  @Test
  public void testDuplicateMethodRoute() throws Exception {
    CompilerAssert<File, File> helper = compiler("metamodel.router.duplicate.methodroute").formalErrorReporting(true);
    List<CompilationError> errors =  helper.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertSame(RouterMetaModel.ROUTER_DUPLICATE_ROUTE, error.getCode());
    assertEquals(Arrays.asList("/foo"), error.getArguments());
    assertEquals("/metamodel/router/duplicate/methodroute/A.java", error.getSource());
  }

  @Test
  public void testParamPattern() throws Exception {
    CompilerAssert<File, File> helper = compiler("metamodel.router.param.pattern").formalErrorReporting(true);
    helper.assertCompile();
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    ModuleMetaModel mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;
    ApplicationMetaModel application = mm.getChild(Key.of(ElementHandle.Package.create(QN.create("metamodel", "router", "param", "pattern")), ApplicationMetaModel.class));
    RouterMetaModel router = application.getChild(Key.of(RouterMetaModel.class));
    RouteMetaModel root = router.getRoot();
    RouteMetaModel route = root.getChildren().get(0);
    assertEquals("/{foo}", route.getPath());
    assertEquals(Collections.singletonMap("foo", ".*"), route.getParameters());
  }
}
