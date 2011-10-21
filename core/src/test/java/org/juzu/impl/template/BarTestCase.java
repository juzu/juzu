/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.juzu.impl.template;

import org.juzu.impl.compiler.CompilationError;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

import java.io.File;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BarTestCase extends AbstractTestCase
{

   public void testResolution() throws Exception
   {
      CompilerHelper<File, RAMPath> compiler = compiler("template", "url", "resolution");
      compiler.assertCompile();
   }

   public void testInvalidMethodName() throws Exception
   {
      CompilerHelper<File, RAMPath> compiler = compiler("template", "url", "invalid_method_name");
      CompilationError error = compiler.failCompile().get(0);
      assertEquals("/template/url/invalid_method_name/A.java", error.getSource());
   }

   public void testInvalidMethodArgs() throws Exception
   {
      CompilerHelper<File, RAMPath> compiler = compiler("template", "url", "invalid_method_args");
      CompilationError error = compiler.failCompile().get(0);
      assertEquals("/template/url/invalid_method_args/A.java", error.getSource());
   }
}
