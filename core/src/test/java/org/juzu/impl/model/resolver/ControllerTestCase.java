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

package org.juzu.impl.model.resolver;

import org.juzu.impl.processor.ModelProcessor;
import org.juzu.impl.utils.Tools;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

import java.io.File;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerTestCase extends AbstractTestCase
{

   public void testGenerateOnce() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "resolver", "controller");
      helper.assertCompile();

      //
      File a = helper.getSourcePath().getPath("model", "resolver", "controller", "A.java");
      File a_ = helper.getSourceOutput().getPath("model", "resolver", "controller", "A_.java");
      long aLast = a.lastModified();
      long a_Last = a_.lastModified();

      // Change the content of the source without changing its last modified date
      Tools.write(Tools.read(a) + " ", a);
      assertTrue(a.setLastModified(aLast));
      assertEquals(aLast, a.lastModified());
      assertTrue(helper.getClassOutput().getPath("model", "resolver", "controller", "A.class").delete());

      //
      helper.with(new ModelProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      assertEquals(a_Last, a_.lastModified());
   }

   public void testRegenerate() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "resolver", "controller");
      helper.assertCompile();

      //
      File a = helper.getSourcePath().getPath("model", "resolver", "controller", "A.java");
      File a_ = helper.getSourceOutput().getPath("model", "resolver", "controller", "A_.java");
      long aLast = a.lastModified();
      long a_Last = a_.lastModified();

      // Change the content of the source without changing its last modified date
      assertTrue(a.setLastModified(aLast + 1000));
      assertEquals(aLast + 1000, a.lastModified());
      assertTrue(helper.getClassOutput().getPath("model", "resolver", "controller", "A.class").delete());

      //
      helper.with(new ModelProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      assertEquals(a_Last, a_.lastModified());
   }
}
