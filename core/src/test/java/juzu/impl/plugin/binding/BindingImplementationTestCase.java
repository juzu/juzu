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

package juzu.impl.plugin.binding;

import juzu.impl.compiler.CompilationError;
import juzu.impl.inject.spi.InjectImplementation;
import juzu.test.AbstractInjectTestCase;
import juzu.test.CompilerAssert;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockRenderBridge;
import org.junit.Test;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BindingImplementationTestCase extends AbstractInjectTestCase {

  public BindingImplementationTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void testNotAssignable() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "binding", "implementation", "notassignable");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(BindingMetaModelPlugin.IMPLEMENTATION_NOT_ASSIGNABLE, error.getCode());
    assertEquals("/plugin/binding/implementation/notassignable/package-info.java", error.getSource());
  }

  @Test
  public void testNotClass() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "binding", "implementation", "notclass");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(BindingMetaModelPlugin.IMPLEMENTATION_INVALID_TYPE, error.getCode());
    assertEquals("/plugin/binding/implementation/notclass/package-info.java", error.getSource());
  }

  @Test
  public void testAbstractClass() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "binding", "implementation", "abstractclass");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(BindingMetaModelPlugin.IMPLEMENTATION_NOT_ABSTRACT, error.getCode());
    assertEquals("/plugin/binding/implementation/abstractclass/package-info.java", error.getSource());
  }

  @Test
  public void testCreate() throws Exception {
    MockApplication<?> app = application("plugin", "binding", "implementation", "create").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("pass", render.assertStringResult());
  }
}
