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
public class ApplicationTestCase extends AbstractTestCase {

  @Test
  public void testAdd() throws Exception {
    CompilerAssert<File, File> helper = compiler("metamodel", "application");
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
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel", "application");
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState b = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)b.metaModel;
    mm.getQueue().clear();
    Tools.serialize(b, ser);

    //
    JavaFile pkgFile = helper.assertJavaFile("metamodel", "application", "package-info.java");
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
    helper.addClassPath(helper.getClassOutput()).assertCompile();
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
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel", "application");
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
    helper.addClassPath(helper.getClassOutput()).assertCompile();
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
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel", "application");
    helper.assertCompile();

    //
    JavaFile pkgFile = helper.assertJavaFile("metamodel", "application", "package-info.java");
    PackageDeclaration pkg = pkgFile.assertPackage();
    pkg.getAnnotations().clear();
    pkgFile.assertSave();

    //
    helper.assertCompile();


  }
}
