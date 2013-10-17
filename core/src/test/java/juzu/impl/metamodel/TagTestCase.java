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

import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import juzu.impl.common.Path;
import juzu.impl.common.Tools;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.plugin.template.metamodel.TemplateMetaModel;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import juzu.test.JavaFile;
import org.junit.Test;

import java.io.File;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TagTestCase extends AbstractTestCase {

  @Test
  public void testUpdateAnnotation() throws Exception {
    CompilerAssert<File, File> helper = compiler("metamodel.tag");
    helper.assertCompile();

    //
    JavaFile file = helper.assertJavaSource("metamodel.tag.package-info");
    PackageDeclaration a = file.assertPackage();
    SingleMemberAnnotationExpr applicationDecl = (SingleMemberAnnotationExpr)a.getAnnotations().get(0);
    NormalAnnotationExpr tagDecl = (NormalAnnotationExpr)applicationDecl.getMemberValue();
    boolean changed = false;
    for (MemberValuePair pair : tagDecl.getPairs()) {
      if (pair.getName().equals("path")) {
        pair.setValue(new StringLiteralExpr("bar.gtmpl"));
        changed = true;
      }
    }
    assertTrue(changed);
    file.assertSave();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState unserialize = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)unserialize.metaModel;
    mm.getQueue().clear();
    Tools.serialize(unserialize, ser);

    //
    helper.assertCompile();
    unserialize = Tools.unserialize(MetaModelState.class, ser);
    mm = (ModuleMetaModel)unserialize.metaModel;
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(2, events.size());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
    assertInstanceOf(TemplateMetaModel.class, events.get(0).getObject());
    assertEquals(Path.parse("/metamodel/tag/tags/foo.gtmpl"), ((TemplateMetaModel)events.get(0).getObject()).getPath());
    assertEquals(MetaModelEvent.AFTER_ADD, events.get(1).getType());
    assertInstanceOf(TemplateMetaModel.class, events.get(1).getObject());
    assertEquals(Path.parse("/metamodel/tag/tags/bar.gtmpl"), ((TemplateMetaModel)events.get(1).getObject()).getPath());
  }

  @Test
  public void testRemoveAnnotation() throws Exception {
    CompilerAssert<File, File> helper = compiler("metamodel.tag");
    helper.assertCompile();

    //
    JavaFile file = helper.assertJavaSource("metamodel.tag.package-info");
    PackageDeclaration a = file.assertPackage();
    a.getAnnotations().remove(0);
    file.assertSave();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState unserialize = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)unserialize.metaModel;
    mm.getQueue().clear();
    Tools.serialize(unserialize, ser);

    //
    helper.assertCompile();
    unserialize = Tools.unserialize(MetaModelState.class, ser);
    mm = (ModuleMetaModel)unserialize.metaModel;
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(1, events.size());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
    assertInstanceOf(TemplateMetaModel.class, events.get(0).getObject());
    assertEquals(Path.parse("/metamodel/tag/tags/foo.gtmpl"), ((TemplateMetaModel)events.get(0).getObject()).getPath());
  }
}
