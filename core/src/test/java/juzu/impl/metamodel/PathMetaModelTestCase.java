/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.metamodel;

import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static juzu.impl.common.JSON.json;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PathMetaModelTestCase extends AbstractTestCase {

  @Test
  public void testBuild() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.path");
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    ModuleMetaModel mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

    //
    JSON expected = json()
      .set("applications", json().
        list("values", json().
          list("controllers").
          set("handle", "ElementHandle.Package[qn=metamodel.path]").
          list("templates", json().
            set("path", "foo.gtmpl").
            list("refs")
          )
        )
      );
    assertEquals(expected, mm.toJSON());
  }

  @Test
  public void testChangeValue() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.path");
    helper.assertCompile();

    //
    File a = helper.getSourcePath().getPath("metamodel", "path", "A.java");
    Tools.write(Tools.read(a).replace("foo.gtmpl", "bar.gtmpl"), a);
    File foo = helper.getSourcePath().getPath("metamodel", "path", "templates", "foo.gtmpl");
    File bar = new File(foo.getParentFile(), "bar.gtmpl");
    assertTrue(foo.renameTo(bar));

    //
    helper.assertCompile();
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    ModuleMetaModel mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

    //
    JSON expected = json()
      .set("applications",
        json().list("values", json().
          list("controllers").
          set("handle", "ElementHandle.Package[qn=metamodel.path]").
          list("templates", json().
              set("path", "bar.gtmpl").
              list("refs")
          )
        )
      );
    assertEquals(expected, mm.toJSON());
  }

  @Test
  public void testRemoveAnnotation() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.path");
    helper.assertCompile();

    //
    File a = helper.getSourcePath().getPath("metamodel", "path", "A.java");
    Tools.write(Tools.read(a).replace("@Path(\"foo.gtmpl\")", ""), a);

    //
    helper.assertCompile();
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    ModuleMetaModel mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

    //
    JSON expected = json()
      .set("applications", json().
        list("values", json().
          list("controllers").
          set("handle", "ElementHandle.Package[qn=metamodel.path]").
          list("templates")
        )
      );
    assertEquals(expected, mm.toJSON());
  }

  @Test
  public void testPathRemoveApplication() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.path");
    helper.assertCompile();

    //
    assertDelete(helper.getSourcePath().getPath("metamodel", "path", "package-info.java"));

    //
    helper.assertCompile();
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    ModuleMetaModel mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

    //
    JSON expected = json().
      set("applications",
        json().list("values")
      );

    //
    assertEquals(expected, mm.toJSON());
  }

  @Test
  public void testRefactorApplication() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.path");
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState unserialize = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)unserialize.metaModel;
    mm.getQueue().clear();
    Tools.serialize(unserialize, ser);

    //
    File pkg = helper.getSourcePath().getPath("metamodel", "path", "package-info.java");
    File dst = new File(pkg.getParentFile().getParentFile(), pkg.getName());
    assertTrue(pkg.renameTo(dst));
    pkg = dst;
    File templates = helper.getSourcePath().getPath("metamodel", "path", "templates");
    File newtemplates = new File(templates.getParentFile().getParentFile(), templates.getName());
    assertTrue(templates.renameTo(newtemplates));
    Tools.write(Tools.read(pkg).replace("package metamodel.path;", "package metamodel;"), pkg);

    //
    helper.assertCompile();
    ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

    //
    JSON expected = json()
      .set("applications", json().
        list("values", json().
          list("controllers").
          set("handle", "ElementHandle.Package[qn=metamodel]").
          list("templates", json().
            set("path", "foo.gtmpl").
            list("refs")
          )
        )
      );
    assertEquals(expected, mm.toJSON());

    // Should also test objects....
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(4, events.size());


    // 1 remove application
    // 2 add application

    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(1).getType());
    assertEquals(MetaModelEvent.AFTER_ADD, events.get(2).getType());
    assertEquals(MetaModelEvent.AFTER_ADD, events.get(3).getType());
  }
}
