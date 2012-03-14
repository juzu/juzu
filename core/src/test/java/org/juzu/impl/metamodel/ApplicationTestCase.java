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
import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.utils.JSON;
import org.juzu.processor.MainProcessor;
import org.juzu.impl.utils.Tools;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

import java.io.File;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationTestCase extends AbstractTestCase
{

   @Test
   public void testAdd() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "application").with(new MainProcessor());
      helper.assertCompile();

      //
      MetaModel mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser"));
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(1, events.size());
      assertEquals(MetaModelEvent.AFTER_ADD, events.get(0).getType());
      assertTrue(events.get(0).getObject() instanceof ApplicationMetaModel);

      //
      JSON expected = new JSON()
         .list("applications", new JSON().
            list("controllers").
            set("fqn", "model.meta.application.ApplicationApplication").
            set("handle", "ElementHandle.Package[qn=model.meta.application]").
            list("templates")
         );
      assertEquals(expected, mm.toJSON());
   }

   @Test
   public void testUpdate() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "application").with(new MainProcessor());
      helper.assertCompile();

      //
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      assertTrue(helper.getClassOutput().getPath("model", "meta", "application", "package-info.class").delete());

      //
      helper.with(new MainProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser"));

      //
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(1, events.size());
      assertEquals(MetaModelEvent.UPDATED, events.get(0).getType());
      assertTrue(events.get(0).getObject() instanceof ApplicationMetaModel);

      //
      JSON expected = new JSON()
         .list("applications", new JSON().
            list("controllers").
            set("fqn", "model.meta.application.ApplicationApplication").
            set("handle", "ElementHandle.Package[qn=model.meta.application]").
            list("templates")
         );
      assertEquals(expected, mm.toJSON());
   }

   @Test
   public void testRemove() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "application").with(new MainProcessor());
      helper.assertCompile();

      //
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      assertTrue(helper.getSourcePath().getPath("model", "meta", "application", "package-info.java").delete());
      assertTrue(helper.getClassOutput().getPath("model", "meta", "application", "package-info.class").delete());
      assertTrue(helper.getClassOutput().getPath("model", "meta", "application", "B.class").delete());

      //
      helper.with(new MainProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser"));

      //
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(1, events.size());
      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
      assertTrue(events.get(0).getObject() instanceof ApplicationMetaModel);

      //
      JSON expected = new JSON().list("applications");
      assertEquals(expected, mm.toJSON());
   }
}
