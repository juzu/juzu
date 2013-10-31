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

import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import juzu.impl.common.Path;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.common.Tools;
import juzu.impl.plugin.template.metamodel.ElementMetaModel;
import juzu.impl.plugin.template.metamodel.TemplateMetaModel;
import juzu.impl.plugin.template.metamodel.TemplateMetaModelPlugin;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import juzu.test.JavaFile;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateMetaModelTestCase extends AbstractTestCase {

  @Test
  public void testTemplatePathMatching() {
    assertNotMatch("a");
    assertMatch("a.b", "", "a", "b");
    assertNotMatch("/a.b");
    assertMatch("a/b.c", "a/", "b", "c");
    assertNotMatch("/a/b.c");
    assertNotMatch("a/b");
  }

  private void assertMatch(String test, String expectedFolder, String expectedRawName, String expectedExtension) {
    Matcher matcher = TemplateMetaModelPlugin.PATH_PATTERN.matcher(test);
    assertTrue("Was expecting " + test + " to match", matcher.matches());
    assertEquals(expectedFolder, matcher.group(1));
    assertEquals(expectedRawName, matcher.group(2));
    assertEquals(expectedExtension, matcher.group(3));
  }

  private void assertNotMatch(String test) {
    Matcher matcher = TemplateMetaModelPlugin.PATH_PATTERN.matcher(test);
    assertFalse("Was not expecting " + test + " to match", matcher.matches());
  }

  @Test
  public void testRemoveTemplate() throws Exception {
    CompilerAssert<File, File> helper = compiler("metamodel.template");
    helper.assertCompile();

    //
    assertDelete(helper.getSourcePath().getPath("metamodel", "template", "templates", "index.gtmpl"));

    //
    helper.failCompile();
  }

  @Test
  public void testRemoveAnnotation() throws Exception {
    CompilerAssert<File, File> helper = compiler("metamodel.template");
    helper.assertCompile();

    //
    JavaFile file = helper.assertJavaSource("metamodel.template.A");
    ClassOrInterfaceDeclaration a = file.assertDeclaration();
    FieldDeclaration decl = (FieldDeclaration)a.getMembers().get(0);
    decl.getAnnotations().clear();
    file.assertSave();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState unserialize = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)unserialize.metaModel;
    mm.getQueue().clear();
    Tools.serialize(unserialize, ser);

    //
    helper.assertCompile();

    //
    unserialize = Tools.unserialize(MetaModelState.class, ser);
    mm = (ModuleMetaModel)unserialize.metaModel;
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(1, events.size());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
    assertInstanceOf(TemplateMetaModel.class, events.get(0).getObject());
  }

  @Test
  public void testReferences() throws Exception {
    ElementHandle.Field templateField = ElementHandle.Field.create("metamodel.template.A", "template");
    CompilerAssert<File, File> helper = compiler("metamodel.template");
    File templates = helper.getSourcePath().getPath("metamodel", "template", "templates");
    File index = new File(templates, "index.gtmpl");
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState unserialize = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)unserialize.metaModel;
    List<MetaModelEvent> events = mm.getQueue().clear();
    Tools.serialize(unserialize, ser);

    //
    assertEquals(2, events.size());
    assertEquals(MetaModelEvent.AFTER_ADD, events.get(1).getType());
    TemplateMetaModel indexTemplate = (TemplateMetaModel)events.get(1).getObject();
    assertEquals(Path.parse("/metamodel/template/templates/index.gtmpl"), indexTemplate.getPath());
    Collection<ElementMetaModel> indexRefs = indexTemplate.getElementReferences();
    assertEquals(1, indexRefs.size());
    assertEquals(templateField, indexRefs.iterator().next().getElement());

    // Create an inclusion
    File foo = new File(templates, "foo.gtmpl");
    assertTrue(foo.createNewFile());
    Tools.safeClose(Tools.copy(new StringReader("#{include path=foo.gtmpl}#{/include}"), new FileWriter(index)));
    assertTrue(index.setLastModified(index.lastModified() + 1000));

    // Compile
    helper.assertCompile();
    unserialize = Tools.unserialize(MetaModelState.class, ser);
    mm = (ModuleMetaModel)unserialize.metaModel;
    events = mm.getQueue().clear();
    Tools.serialize(unserialize, ser);

    //
    assertEquals(1, events.size());
    assertEquals(MetaModelEvent.AFTER_ADD, events.get(0).getType());
    TemplateMetaModel fooTemplate = (TemplateMetaModel)events.get(0).getObject();
    assertEquals(Path.parse("/metamodel/template/templates/foo.gtmpl"), fooTemplate.getPath());
    Collection<ElementMetaModel> fooRefs = fooTemplate.getElementReferences();
    assertEquals(1, fooRefs.size());
    assertEquals(templateField, fooRefs.iterator().next().getElement());

    // Create an neted inclusion
    File bar = new File(templates, "bar.gtmpl");
    assertTrue(bar.createNewFile());
    Tools.safeClose(Tools.copy(new StringReader("#{include path=bar.gtmpl}#{/include}"), new FileWriter(foo)));
    assertTrue(foo.setLastModified(index.lastModified() + 1000));

    // Compile
    helper.assertCompile();
    unserialize = Tools.unserialize(MetaModelState.class, ser);
    mm = (ModuleMetaModel)unserialize.metaModel;
    events = mm.getQueue().clear();

    //
    assertEquals(1, events.size());
    assertEquals(MetaModelEvent.AFTER_ADD, events.get(0).getType());
    TemplateMetaModel barTemplate = (TemplateMetaModel)events.get(0).getObject();
    assertEquals(Path.parse("/metamodel/template/templates/bar.gtmpl"), barTemplate.getPath());
    Collection<ElementMetaModel> barRefs = fooTemplate.getElementReferences();
    assertEquals(1, barRefs.size());
    assertEquals(templateField, barRefs.iterator().next().getElement());
  }
}
