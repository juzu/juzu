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

package org.juzu.impl.metamodel;

import org.juzu.Phase;
import org.juzu.impl.utils.Tools;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerTestCase extends AbstractTestCase
{

   public void testBuild() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("metamodel", "controller", "simple").with(new MetaModelProcessor());
      helper.assertCompile();

      //
      MetaModel mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "model2.ser"));

      //
      MetaModel expected = new MetaModel();
      ApplicationMetaModel application = expected.addApplication("metamodel.controller.simple", "SimpleApplication");
      ControllerMetaModel controller = expected.addController("metamodel.controller.simple.A");
      controller.addMethod(Phase.RENDER, "index", Collections.<Map.Entry<String, String>>emptyList());
      application.addController(controller);
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void testRemoveApplication() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("metamodel", "controller", "simple").with(new MetaModelProcessor());
      helper.assertCompile();

      //
      assertDelete(helper.getSourcePath().getPath("metamodel", "controller", "simple", "A.java"));
      assertDelete(helper.getSourcePath().getPath("metamodel", "controller", "simple", "package-info.java"));
      assertDelete(helper.getClassOutput().getPath("metamodel", "controller", "simple", "package-info.class"));

      //
      helper.with(new MetaModelProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      MetaModel mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "model2.ser"));

      //
      MetaModel expected = new MetaModel();
      expected.addController("metamodel.controller.simple.A").addMethod(Phase.RENDER, "index", Collections.<Map.Entry<String, String>>emptyList());
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void testRemoveAnnotation() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("metamodel", "controller", "simple").with(new MetaModelProcessor());
      helper.assertCompile();

      //
      File a = helper.getSourcePath().getPath("metamodel", "controller", "simple", "A.java");
      Tools.write(Tools.read(a).replace("@View", ""), a);
      assertDelete(helper.getSourcePath().getPath("metamodel", "controller", "simple", "package-info.java"));

      //
      helper.with(new MetaModelProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      MetaModel mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "model2.ser"));

      //
      MetaModel expected = new MetaModel();
      expected.addApplication("metamodel.controller.simple", "SimpleApplication");
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void testRemoveControllerMethod() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("metamodel", "controller", "simple").with(new MetaModelProcessor());
      helper.assertCompile();

      //
      File a = helper.getSourcePath().getPath("metamodel", "controller", "simple", "A.java");
      Tools.write(Tools.read(a).replace("@View\n   public void index()\n   {\n   }", ""), a);
      assertDelete(helper.getSourcePath().getPath("metamodel", "controller", "simple", "package-info.java"));

      //
      helper.with(new MetaModelProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      MetaModel mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "model2.ser"));

      //
      MetaModel expected = new MetaModel();
      expected.addApplication("metamodel.controller.simple", "SimpleApplication");
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void testChangeAnnotation() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("metamodel", "controller", "simple").with(new MetaModelProcessor());
      helper.assertCompile();

      //
      File a = helper.getSourcePath().getPath("metamodel", "controller", "simple", "A.java");
      Tools.write(Tools.read(a).replace("View", "Action"), a);
      assertDelete(helper.getSourcePath().getPath("metamodel", "controller", "simple", "package-info.java"));

      //
      helper.with(new MetaModelProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      MetaModel mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "model2.ser"));

      //
      MetaModel expected = new MetaModel();
      ApplicationMetaModel application = expected.addApplication("metamodel.controller.simple", "SimpleApplication");
      ControllerMetaModel controller = expected.addController("metamodel.controller.simple.A");
      controller.addMethod(Phase.ACTION, "index", Collections.<Map.Entry<String, String>>emptyList());
      application.addController(controller);
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void testUpdateController() throws Exception
   {
      // todo
   }
}
