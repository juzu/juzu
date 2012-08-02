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

import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.common.Tools;
import juzu.impl.compiler.CompilationError;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.ram.RAMFileSystem;
import juzu.impl.fs.spi.ram.RAMPath;
import juzu.impl.plugin.template.metamodel.TemplateMetaModel;
import juzu.impl.plugin.template.metamodel.TemplateMetaModelPlugin;
import juzu.impl.common.Content;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import juzu.test.JavaFile;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateTestCase extends AbstractTestCase {

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

  public void _testSimpleIncremental() throws Exception {
    DiskFileSystem fs = diskFS("processor", "simple");

    //
    RAMFileSystem sourcePath = new RAMFileSystem();
    fs.copy(sourcePath);
    sourcePath.getPath("processor", "simple", "templates", "index.gtmpl").del();
    sourcePath.getPath("processor", "simple", "package-info.java").del();

    //
    RAMFileSystem sourceOutput = new RAMFileSystem();
    RAMFileSystem classOutput = new RAMFileSystem();

    //
    CompilerAssert<RAMPath, RAMPath> helper = new CompilerAssert<RAMPath, RAMPath>(sourcePath, sourceOutput, classOutput);
    helper.assertCompile();
    assertEquals(2, classOutput.size(ReadFileSystem.FILE));
    assertNotNull(classOutput.getPath("juzu", "config.properties"));
    assertNotNull(classOutput.getPath("processor", "simple", "A.class"));

    //
    fs.copy(sourcePath);
    sourcePath.getPath("processor", "simple", "templates", "index.gtmpl").del();
    helper = new CompilerAssert<RAMPath, RAMPath>(sourcePath, sourceOutput, classOutput);

    //
    List<CompilationError> errors = helper.failCompile();
    assertEquals(1, errors.size());
    CompilationError error = errors.get(0);
    assertEquals(TemplateMetaModel.TEMPLATE_NOT_RESOLVED.toString(), error.getCode());
    assertEquals(2, classOutput.size(ReadFileSystem.FILE));
    assertNotNull(classOutput.getPath("juzu", "config.properties"));
    assertNotNull(classOutput.getPath("processor", "simple", "A.class"));

    //
    fs.copy(sourcePath);
    sourcePath.getPath("processor", "simple", "A.java").del();
//      sourceOutput.getPath("processor", "simple", "A_.java").del();
    classOutput.getPath("processor", "simple", "A.class").del();
    helper = new CompilerAssert<RAMPath, RAMPath>(sourcePath, sourceOutput, classOutput);

    // This test cannot pass actually
//      helper.assertCompile();
//      assertEquals(9, classOutput.size(ReadFileSystem.FILE));
//      assertNotNull(classOutput.getPath("juzu", "config.properties"));
//      assertNotNull(classOutput.getPath("processor", "simple", "templates", "index.groovy"));
//      assertNotNull(classOutput.getPath("processor", "simple", "config.properties"));
//      assertNotNull(classOutput.getPath("processor", "simple", "package-info.class"));
//      assertNotNull(classOutput.getPath("processor", "simple", "SimpleApplication.class"));
//      assertNotNull(classOutput.getPath("processor", "simple", "A.class"));
//      assertNotNull(classOutput.getPath("processor", "simple", "A_.class"));
  }

  public void _testModifyTemplate() throws Exception {
    DiskFileSystem fs = diskFS("processor", "simple");

    //
    RAMFileSystem sourcePath = new RAMFileSystem();
    fs.copy(sourcePath);

    //
    RAMFileSystem sourceOutput = new RAMFileSystem();
    RAMFileSystem classOutput = new RAMFileSystem();

    //
    CompilerAssert<RAMPath, RAMPath> compiler = new CompilerAssert<RAMPath, RAMPath>(sourcePath, sourceOutput, classOutput);

    //
    compiler.assertCompile();
    assertEquals(9, classOutput.size(ReadFileSystem.FILE));
    assertNotNull(classOutput.getPath("juzu", "config.properties"));
    assertNotNull(classOutput.getPath("processor", "simple", "templates", "index.groovy"));
    assertNotNull(classOutput.getPath("processor", "simple", "config.properties"));
    assertNotNull(classOutput.getPath("processor", "simple", "package-info.class"));
    assertNotNull(classOutput.getPath("processor", "simple", "SimpleApplication.class"));
    assertNotNull(classOutput.getPath("processor", "simple", "A.class"));
    assertNotNull(classOutput.getPath("processor", "simple", "A_.class"));

    // We force a regeneration of the template by removing the class A
    Content c1 = classOutput.getPath("processor", "simple", "templates", "index.groovy").getContent();
    sourcePath.getPath("processor", "simple", "templates", "index.gtmpl").update("foo");
    classOutput.getPath("processor", "simple", "A.class").del();
    classOutput.getPath("processor", "simple", "A_.class").del();

    //
    compiler = new CompilerAssert<RAMPath, RAMPath>(sourcePath, sourceOutput, classOutput);
    compiler.assertCompile();
    assertEquals(9, classOutput.size(ReadFileSystem.FILE));
    assertNotNull(classOutput.getPath("juzu", "config.properties"));
    assertNotNull(classOutput.getPath("processor", "simple", "templates", "index.groovy"));
    assertNotNull(classOutput.getPath("processor", "simple", "config.properties"));
    assertNotNull(classOutput.getPath("processor", "simple", "package-info.class"));
    assertNotNull(classOutput.getPath("processor", "simple", "SimpleApplication.class"));
    assertNotNull(classOutput.getPath("processor", "simple", "A.class"));
    assertNotNull(classOutput.getPath("processor", "simple", "A_.class"));

    //
    Content c2 = classOutput.getPath("processor", "simple", "templates", "index.groovy").getContent();
    assertFalse("Was not expecting templates to be identical", c1.getCharSequence().toString().equals(c2.getCharSequence().toString()));
  }

  @Test
  public void testRemoveTemplate() throws Exception {
    CompilerAssert<File, File> helper = compiler("metamodel", "template");
    helper.assertCompile();

    //
    assertDelete(helper.getSourcePath().getPath("metamodel", "template", "templates", "index.gtmpl"));

    //
    helper.addClassPath(helper.getClassOutput()).failCompile();
  }

  @Test
  public void testRemoveAnnotation() throws Exception {
    CompilerAssert<File, File> helper = compiler("metamodel", "template");
    helper.assertCompile();

    //
    JavaFile file = helper.assertJavaFile("metamodel", "template", "A.java");
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
    helper.addClassPath(helper.getClassOutput()).assertCompile();

    //
    unserialize = Tools.unserialize(MetaModelState.class, ser);
    mm = (ModuleMetaModel)unserialize.metaModel;
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(1, events.size());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
    assertInstanceOf(TemplateMetaModel.class, events.get(0).getObject());
  }
}
