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

import org.junit.Test;
import org.juzu.impl.controller.metamodel.ControllerMetaModelPlugin;
import org.juzu.impl.template.metamodel.TemplateMetaModelPlugin;
import org.juzu.impl.utils.JSON;
import org.juzu.processor.MainProcessor;
import org.juzu.impl.utils.Tools;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

import java.io.File;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PathTestCase extends AbstractTestCase
{

   @Test
   public void testBuild() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "path").with(new MainProcessor());
      helper.assertCompile();

      //
      MetaModel mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser"));

      //
      JSON expected = new JSON()
         .list("applications", new JSON().
            list("controllers").
            set("fqn", "model.meta.path.PathApplication").
            set("handle", "ElementHandle.Package[qn=model.meta.path]").
            list("templates", new JSON().
               set("path", "foo.gtmpl").
               list("refs")
            )
         );
      assertEquals(expected, mm.toJSON());
   }

   @Test
   public void testChangeValue() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "path").with(new MainProcessor());
      helper.assertCompile();

      //
      File a = helper.getSourcePath().getPath("model", "meta", "path", "A.java");
      Tools.write(Tools.read(a).replace("foo.gtmpl", "bar.gtmpl"), a);
      File foo = helper.getSourcePath().getPath("model", "meta", "path", "templates", "foo.gtmpl");
      File bar = new File(foo.getParentFile(), "bar.gtmpl");
      assertTrue(foo.renameTo(bar));
      assertDelete(helper.getSourcePath().getPath("model", "meta", "path", "package-info.java"));
      assertDelete(helper.getClassOutput().getPath("model", "meta", "path", "A.class"));

      //
      helper.with(new MainProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      MetaModel mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser"));

      //
      JSON expected = new JSON()
         .list("applications", new JSON().
            list("controllers").
            set("fqn", "model.meta.path.PathApplication").
            set("handle", "ElementHandle.Package[qn=model.meta.path]").
            list("templates", new JSON().
               set("path", "bar.gtmpl").
               list("refs")
            )
         );
      assertEquals(expected, mm.toJSON());
   }

   @Test
   public void testRemoveAnnotation() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "path").with(new MainProcessor());
      helper.assertCompile();

      //
      File a = helper.getSourcePath().getPath("model", "meta", "path", "A.java");
      Tools.write(Tools.read(a).replace("@Path(\"foo.gtmpl\")", ""), a);
      assertDelete(helper.getSourcePath().getPath("model", "meta", "path", "package-info.java"));
      assertDelete(helper.getClassOutput().getPath("model", "meta", "path", "A.class"));

      //
      helper.with(new MainProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      MetaModel mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser"));

      //
      JSON expected = new JSON()
         .list("applications", new JSON().
            list("controllers").
            set("fqn", "model.meta.path.PathApplication").
            set("handle", "ElementHandle.Package[qn=model.meta.path]").
            list("templates")
         );
      assertEquals(expected, mm.toJSON());
   }

   @Test
   public void testPathRemoveApplication() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "path").with(new MainProcessor());
      helper.assertCompile();

      //
      assertDelete(helper.getSourcePath().getPath("model", "meta", "path", "package-info.java"));
      assertDelete(helper.getClassOutput().getPath("model", "meta", "path", "package-info.class"));
      assertDelete(helper.getClassOutput().getPath("model", "meta", "path", "A.class"));

      //
      helper.with(new MainProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      MetaModel mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser"));

      //
      MetaModel expected = new MetaModel();
      expected.addPlugin("controller", new ControllerMetaModelPlugin());
      expected.addPlugin("template", new TemplateMetaModelPlugin());
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   @Test
   public void testRefactorApplication() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "path").with(new MainProcessor());
      helper.assertCompile();

      //
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      File pkg = helper.getSourcePath().getPath("model", "meta", "path", "package-info.java");
      File dst = new File(pkg.getParentFile().getParentFile(), pkg.getName());
      assertTrue(pkg.renameTo(dst));
      pkg = dst;
      File templates = helper.getSourcePath().getPath("model", "meta", "path", "templates");
      File newtemplates = new File(templates.getParentFile().getParentFile(), templates.getName());
      assertTrue(templates.renameTo(newtemplates));
      Tools.write(Tools.read(pkg).replace("package model.meta.path;", "package model.meta;"), pkg);

      //
      assertTrue(helper.getSourcePath().getPath("model", "meta", "path", "A.java").delete());
      assertTrue(helper.getClassOutput().getPath("model", "meta", "path", "package-info.class").delete());

      //
      helper.with(new MainProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser"));

      //
      JSON expected = new JSON()
         .list("applications", new JSON().
            list("controllers").
            set("fqn", "model.meta.MetaApplication").
            set("handle", "ElementHandle.Package[qn=model.meta]").
            list("templates", new JSON().
               set("path", "foo.gtmpl").
               list("refs")
            )
         );
      assertEquals(expected, mm.toJSON());

      // Should also test objects....
      List<MetaModelEvent> events = mm.getQueue().clear();
      System.out.println("events = " + events);
      assertEquals(4, events.size());

      // 1 remove application
      // 2 add application

      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(1).getType());
      assertEquals(MetaModelEvent.AFTER_ADD, events.get(2).getType());
      assertEquals(MetaModelEvent.AFTER_ADD, events.get(3).getType());
   }
}
