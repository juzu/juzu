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

import japa.parser.ASTHelper;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import juzu.Application;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import juzu.test.JavaFile;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static juzu.impl.common.JSON.json;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationMetaModelTestCase extends AbstractTestCase {

  @Test
  public void testAdd() throws Exception {
    CompilerAssert<File, File> helper = compiler("metamodel.application");
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    ModuleMetaModel mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(1, events.size());
    assertEquals(MetaModelEvent.AFTER_ADD, events.get(0).getType());
    assertTrue(events.get(0).getObject() instanceof ApplicationMetaModel);

    //
    JSON expected = json()
      .set("applications", json().
        list("values", json().
          list("controllers").
          set("handle", "ElementHandle.Package[qn=metamodel.application]").
          list("templates")
        )
      );
    assertEquals(expected, mm.toJSON());
  }

  @Test
  public void testUpdate() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.application");
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState b = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)b.metaModel;
    mm.getQueue().clear();
    Tools.serialize(b, ser);

    //
    JavaFile pkgFile = helper.assertJavaSource("metamodel.application.package-info");
    PackageDeclaration pkg = pkgFile.assertPackage();
    pkg.getAnnotations().clear();
    List<AnnotationExpr> a = Collections.<AnnotationExpr>singletonList(new NormalAnnotationExpr(ASTHelper.createNameExpr(
        Application.class.getName()),
        Collections.<MemberValuePair>singletonList(new MemberValuePair(
            "name", new StringLiteralExpr("abc")
        ))));
    pkg.setAnnotations(a);
    pkgFile.assertSave();

    //
    helper.assertCompile();
    ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

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
          set("handle", "ElementHandle.Package[qn=metamodel.application]").
          list("templates")
        )
      );
    assertEquals(expected, mm.toJSON());
  }

  @Test
  public void testRemove() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.application");
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState unserialize = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)unserialize.metaModel;
    mm.getQueue().clear();
    Tools.serialize(unserialize, ser);

    //
    assertTrue(helper.getSourcePath().getPath("metamodel", "application", "package-info.java").delete());

    //
    helper.assertCompile();
    ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

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

  @Test
  public void testRemoveAnnotation() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.application");
    helper.assertCompile();

    //
    JavaFile pkgFile = helper.assertJavaSource("metamodel.application.package-info");
    PackageDeclaration pkg = pkgFile.assertPackage();
    pkg.getAnnotations().clear();
    pkgFile.assertSave();

    //
    helper.assertCompile();


  }
}
