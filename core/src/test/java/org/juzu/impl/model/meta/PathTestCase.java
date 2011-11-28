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

import org.juzu.impl.utils.Tools;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

import java.io.File;

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
      TemplateMetaModel template = application.addTemplate(ref);
      template.addRef(ref);
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
      TemplateMetaModel template = application.addTemplate(ref);
      template.addRef(ref);
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
}
