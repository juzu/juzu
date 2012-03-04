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

package org.juzu.impl.model.meta;

import org.juzu.impl.model.meta.template.TemplateMetaModel;
import org.juzu.impl.model.meta.template.TemplateRefMetaModel;
import org.juzu.impl.utils.Tools;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

import java.io.File;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PathTestCase extends AbstractTestCase
{

   public void testBuild() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "path").with(new MetaModelProcessor());
      helper.assertCompile();

      //
      MetaModel mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "model2.ser"));

      //
      MetaModel expected = new MetaModel();
      ApplicationMetaModel application = expected.addApplication("model.meta.path", "PathApplication");
      TemplateRefMetaModel ref = expected.addTemplateRef("model.meta.path.A", "index", "foo.gtmpl");
      TemplateMetaModel template = application.getTemplates().add(ref);
      ref.setTemplate(template);
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void testChangeValue() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "path").with(new MetaModelProcessor());
      helper.assertCompile();

      //
      File a = helper.getSourcePath().getPath("model", "meta", "path", "A.java");
      Tools.write(Tools.read(a).replace("foo.gtmpl", "bar.gtmpl"), a);
      assertDelete(helper.getSourcePath().getPath("model", "meta", "path", "package-info.java"));
      assertDelete(helper.getClassOutput().getPath("model", "meta", "path", "A.class"));

      //
      helper.with(new MetaModelProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      MetaModel mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "model2.ser"));

      //
      MetaModel expected = new MetaModel();
      ApplicationMetaModel application = expected.addApplication("model.meta.path", "PathApplication");
      TemplateRefMetaModel ref = expected.addTemplateRef("model.meta.path.A", "index", "bar.gtmpl");
      TemplateMetaModel template = application.getTemplates().add(ref);
      ref.setTemplate(template);
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void testRemoveAnnotation() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "path").with(new MetaModelProcessor());
      helper.assertCompile();

      //
      File a = helper.getSourcePath().getPath("model", "meta", "path", "A.java");
      Tools.write(Tools.read(a).replace("@Path(\"foo.gtmpl\")", ""), a);
      assertDelete(helper.getSourcePath().getPath("model", "meta", "path", "package-info.java"));
      assertDelete(helper.getClassOutput().getPath("model", "meta", "path", "A.class"));

      //
      helper.with(new MetaModelProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      MetaModel mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "model2.ser"));

      //
      MetaModel expected = new MetaModel();
      expected.addApplication("model.meta.path", "PathApplication");
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void testPathRemoveApplication() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "path").with(new MetaModelProcessor());
      helper.assertCompile();

      //
      assertDelete(helper.getSourcePath().getPath("model", "meta", "path", "package-info.java"));
      assertDelete(helper.getClassOutput().getPath("model", "meta", "path", "package-info.class"));
      assertDelete(helper.getClassOutput().getPath("model", "meta", "path", "A.class"));

      //
      helper.with(new MetaModelProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      MetaModel mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "model2.ser"));

      //
      MetaModel expected = new MetaModel();
      expected.addTemplateRef("model.meta.path.A", "index", "foo.gtmpl");
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void testRefactorApplication() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "path").with(new MetaModelProcessor());
      helper.assertCompile();

      //
      File ser = helper.getSourceOutput().getPath("org", "juzu", "model2.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.popEvents();
      Tools.serialize(mm, ser);

      //
      File f = helper.getSourcePath().getPath("model", "meta", "path", "package-info.java");
      File dst = new File(f.getParentFile().getParentFile(), f.getName());
      assertTrue(f.renameTo(dst));
      f = dst;
      Tools.write(Tools.read(f).replace("package model.meta.path;", "package model.meta;"), f);

      //
      assertTrue(helper.getSourcePath().getPath("model", "meta", "path", "A.java").delete());
      assertTrue(helper.getClassOutput().getPath("model", "meta", "path", "package-info.class").delete());

      //
      helper.with(new MetaModelProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "model2.ser"));

      //
      MetaModel expected = new MetaModel();
      ApplicationMetaModel application = expected.addApplication("model.meta", "MetaApplication");
      TemplateRefMetaModel ref = expected.addTemplateRef("model.meta.path.A", "index", "foo.gtmpl");
      TemplateMetaModel template = application.getTemplates().add(ref);
      ref.setTemplate(template);
      assertEquals(expected.toJSON(), mm.toJSON());

      // Should also test objects....
      List<MetaModelEvent> events = mm.popEvents();
      assertEquals(4, events.size());

      // 1 remove application
      // 2 add application

      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(1).getType());
      assertEquals(MetaModelEvent.AFTER_ADD, events.get(2).getType());
      assertEquals(MetaModelEvent.AFTER_ADD, events.get(3).getType());
   }
}
