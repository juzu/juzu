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

import juzu.impl.plugin.application.ApplicationException;
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
public class BindingProviderFactoryTestCase extends AbstractInjectTestCase {

  public BindingProviderFactoryTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void testNoPublicCtor() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "binding", "provider", "factory", "nopublicctor");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(BindingMetaModelPlugin.PROVIDER_FACTORY_NO_PUBLIC_CTOR, error.getCode());
    assertEquals("/plugin/binding/provider/factory/nopublicctor/package-info.java", error.getSource());
  }

  @Test
  public void testNoZeroCtor() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "binding", "provider", "factory", "nozeroargctor");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(BindingMetaModelPlugin.PROVIDER_FACTORY_NO_ZERO_ARG_CTOR, error.getCode());
    assertEquals("/plugin/binding/provider/factory/nozeroargctor/package-info.java", error.getSource());
  }

  @Test
  public void testAbstractClass() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "binding", "provider", "factory", "abstractclass");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(BindingMetaModelPlugin.IMPLEMENTATION_NOT_ABSTRACT, error.getCode());
    assertEquals("/plugin/binding/provider/factory/abstractclass/package-info.java", error.getSource());
  }

  @Test
  public void testNotPublicClass() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "binding", "provider", "factory", "notpublicclass");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(BindingMetaModelPlugin.PROVIDER_FACTORY_NOT_PUBLIC, error.getCode());
    assertEquals("/plugin/binding/provider/factory/notpublicclass/package-info.java", error.getSource());
  }

  @Test
  public void testNotClass() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "binding", "provider", "factory", "notclass");
    compiler.formalErrorReporting(true);
    List<CompilationError> errors = compiler.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(BindingMetaModelPlugin.IMPLEMENTATION_INVALID_TYPE, error.getCode());
    assertEquals("/plugin/binding/provider/factory/notclass/package-info.java", error.getSource());
  }

  @Test
  public void testCreate() throws Exception {
    MockApplication<?> app = application("plugin", "binding", "provider", "factory", "create").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("pass", render.assertStringResult());
  }

  @Test
  public void testGetProviderThrowable() throws Exception {
    MockApplication<?> app = application("plugin", "binding", "provider", "factory", "throwable");

    //
    try {
      app.init();
      fail();
    }
    catch (ApplicationException e) {
      assertInstanceOf(SecurityException.class, e.getCause());
    }
  }
}
