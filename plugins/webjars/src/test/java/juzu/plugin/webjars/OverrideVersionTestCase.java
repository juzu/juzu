/*
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
package juzu.plugin.webjars;

import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import org.junit.Test;
import juzu.impl.compiler.Compiler;

import java.io.File;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class OverrideVersionTestCase extends AbstractTestCase {
  @Test
  public void test() throws Exception {
    CompilerAssert<File,File> compilerAssert = compiler("juzu.overrideversion");
    Compiler compiler = compilerAssert.assertCompile();
    File root = compilerAssert.getClassOutput().getRoot();
    File resource = new File(root, "juzu/overrideversion/assets/ui-bootstrap.js");
    assertTrue(resource.exists());
    assertTrue(resource.isFile());
  }
}
