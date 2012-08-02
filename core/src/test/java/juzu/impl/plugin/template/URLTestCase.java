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

package juzu.impl.plugin.template;

import juzu.impl.compiler.CompilationError;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import org.junit.Test;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class URLTestCase extends AbstractTestCase {

  @Test
  public void testResolution() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "template", "url", "resolution");
    compiler.assertCompile();
  }

  @Test
  public void testInvalidMethodName() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "template", "url", "invalid_method_name");
    List<CompilationError> errors = compiler.failCompile();
    assertEquals("Was expecting 1 error instead of " + errors, 1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals("/plugin/template/url/invalid_method_name/A.java", error.getSource());
  }

  @Test
  public void testInvalidMethodArgs() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "template", "url", "invalid_method_args");
    List<CompilationError> errors = compiler.failCompile();
    assertEquals("Was expecting 1 error instead of " + errors, 1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals("/plugin/template/url/invalid_method_args/A.java", error.getSource());
  }

  @Test
  public void testOverload() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "template", "url", "overload");
    compiler.assertCompile();
  }
}
