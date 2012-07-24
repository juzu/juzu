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

package juzu.impl.metamodel;

import juzu.impl.application.metamodel.ApplicationMetaModel;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static juzu.impl.common.JSON.json;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationTestCase extends AbstractTestCase {

  @Test
  public void testAdd() throws Exception {
    CompilerAssert<File, File> helper = compiler("model", "meta", "application");
    helper.assertCompile();

    //
    MetaModel mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("juzu", "metamodel.ser"));
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(1, events.size());
    assertEquals(MetaModelEvent.AFTER_ADD, events.get(0).getType());
    assertTrue(events.get(0).getObject() instanceof ApplicationMetaModel);

    //
    JSON expected = json()
      .set("applications", json().
        list("values", json().
          list("controllers").
          set("fqn", "model.meta.application.ApplicationApplication").
          set("handle", "ElementHandle.Package[qn=model.meta.application]").
          list("templates")
        )
      );
    assertEquals(expected, mm.toJSON());
  }

  @Test
  public void testUpdate() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("model", "meta", "application");
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModel mm = Tools.unserialize(MetaModel.class, ser);
    mm.getQueue().clear();
    Tools.serialize(mm, ser);

    // Just touch this file to force recompile
    File pkg = helper.getSourcePath().getPath("model", "meta", "application", "package-info.java");
    FileWriter writer = new FileWriter(pkg, true);
    writer.write(" ");
    writer.close();

    //
    helper.addClassPath(helper.getClassOutput()).assertCompile();
    mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("juzu", "metamodel.ser"));

    //
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(1, events.size());
    assertEquals(MetaModelEvent.UPDATED, events.get(0).getType());
    assertTrue(events.get(0).getObject() instanceof ApplicationMetaModel);

    //
    JSON expected = json()
      .set("applications", json().
        list("values", json().
          list("controllers").
          set("fqn", "model.meta.application.ApplicationApplication").
          set("handle", "ElementHandle.Package[qn=model.meta.application]").
          list("templates")
        )
      );
    assertEquals(expected, mm.toJSON());
  }

  @Test
  public void testRemove() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("model", "meta", "application");
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModel mm = Tools.unserialize(MetaModel.class, ser);
    mm.getQueue().clear();
    Tools.serialize(mm, ser);

    //
    assertTrue(helper.getSourcePath().getPath("model", "meta", "application", "package-info.java").delete());

    //
    helper.addClassPath(helper.getClassOutput()).assertCompile();
    mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("juzu", "metamodel.ser"));

    //
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(1, events.size());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
    assertTrue(events.get(0).getObject() instanceof ApplicationMetaModel);

    //
    JSON expected = json().
      set("applications", json().
        list("values")
      );
    assertEquals(expected, mm.toJSON());
  }
}
