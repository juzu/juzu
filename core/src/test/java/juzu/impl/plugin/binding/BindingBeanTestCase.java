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
public class BindingBeanTestCase extends AbstractInjectTestCase {

  public BindingBeanTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void testCreate() throws Exception {
    MockApplication<?> app = application("plugin", "binding", "create").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("pass", render.assertStringResult());
  }

  @Test
  public void testAbstractClass() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "binding", "abstractclass");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(BindingMetaModelPlugin.BEAN_ABSTRACT_TYPE, error.getCode());
    assertEquals("/plugin/binding/abstractclass/package-info.java", error.getSource());
  }

  @Test
  public void testNotClass() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "binding", "notclass");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(BindingMetaModelPlugin.BEAN_INVALID_TYPE, error.getCode());
    assertEquals("/plugin/binding/notclass/package-info.java", error.getSource());
  }

  @Test
  public void testScope() throws Exception {
    MockApplication<?> app = application("plugin", "binding", "scope").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    String url = render.assertStringResult();
    assertNotSame("", url);

    //
    render = (MockRenderBridge)client.invoke(url);
    String result = render.assertStringResult();
    assertEquals("pass", result);

  }
}
