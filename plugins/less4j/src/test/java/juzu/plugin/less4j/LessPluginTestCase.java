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

package juzu.plugin.less4j;

import juzu.impl.compiler.CompilationError;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.common.Tools;
import juzu.plugin.less4j.impl.MetaModelPluginImpl;
import juzu.test.AbstractInjectTestCase;
import juzu.test.CompilerAssert;
import org.junit.Test;

import java.io.File;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LessPluginTestCase extends AbstractInjectTestCase {

  public LessPluginTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testCompile() throws Exception {
    CompilerAssert<File, File> ca = compiler("plugin.less4j.compile");
    ca.assertCompile();
    File f = ca.getClassOutput().getPath("plugin", "less4j", "compile", "assets", "stylesheet.css");
    assertNotNull(f);
    assertTrue(f.exists());
  }

  @Test
  public void testFail() throws Exception {
    CompilerAssert<File, File> ca = compiler("plugin.less4j.fail");
    List<CompilationError> errors = ca.formalErrorReporting(true).failCompile();
    assertEquals(1, errors.size());
    assertEquals(MetaModelPluginImpl.COMPILATION_ERROR, errors.get(0).getCode());
    File f = ca.getSourcePath().getPath("plugin", "less4j", "fail", "package-info.java");
    assertEquals(f, errors.get(0).getSourceFile());
    f = ca.getClassOutput().getPath("plugin", "less4j", "fail", "assets", "stylesheet.css");
    assertNull(f);
  }

  @Test
  public void testNotFound() throws Exception {
    CompilerAssert<File, File> ca = compiler("plugin.less4j.notfound");
    List<CompilationError> errors = ca.formalErrorReporting(true).failCompile();
    assertEquals(1, errors.size());
    assertEquals(MetaModelPluginImpl.GENERAL_PROBLEM, errors.get(0).getCode());
    File f = ca.getSourcePath().getPath("plugin", "less4j", "notfound", "package-info.java");
    assertEquals(f, errors.get(0).getSourceFile());
    f = ca.getClassOutput().getPath("plugin", "less4j", "notfound", "assets", "stylesheet.css");
    assertNull(f);
  }

  @Test
  public void testResolve() throws Exception {
    CompilerAssert<File, File> ca = compiler("plugin.less4j.resolve");
    ca.assertCompile();
    File f = ca.getClassOutput().getPath("plugin", "less4j", "resolve", "assets", "stylesheet.css");
    assertNotNull(f);
    assertTrue(f.exists());
  }

  @Test
  public void testCannotResolve() throws Exception {
    CompilerAssert<File, File> ca = compiler("plugin.less4j.cannotresolve");
    List<CompilationError> errors = ca.formalErrorReporting(true).failCompile();
    assertEquals(1, errors.size());
    assertEquals(MetaModelPluginImpl.COMPILATION_ERROR, errors.get(0).getCode());
    File f = ca.getSourcePath().getPath("plugin", "less4j", "cannotresolve", "package-info.java");
    assertEquals(f, errors.get(0).getSourceFile());
    f = ca.getClassOutput().getPath("plugin", "less4j", "cannotresolve", "assets", "stylesheet.css");
    assertNull(f);
  }

  @Test
  public void testMalformedPath() throws Exception {
    CompilerAssert<File, File> ca = compiler("plugin.less4j.malformedpath");
    List<CompilationError> errors = ca.formalErrorReporting(true).failCompile();
    assertEquals(1, errors.size());
    assertEquals(MetaModelPluginImpl.MALFORMED_PATH, errors.get(0).getCode());
    File f = ca.getSourcePath().getPath("plugin", "less4j", "malformedpath", "package-info.java");
    assertEquals(f, errors.get(0).getSourceFile());
    f = ca.getClassOutput().getPath("plugin", "less4j", "malformedpath", "assets", "stylesheet.css");
    assertNull(f);
  }

  @Test
  public void testAncestor() throws Exception {
    CompilerAssert<File, File> ca = compiler("plugin.less4j.ancestor");
    ca.assertCompile();
    File f = ca.getClassOutput().getPath("plugin", "less4j", "ancestor", "assets", "folder", "stylesheet.css");
    assertNotNull(f);
    assertTrue(f.exists());
  }

  @Test
  public void testBootstrap() throws Exception {
    CompilerAssert<File, File> ca = compiler("plugin.less4j.bootstrap");
    ca.assertCompile();
    File f = ca.getClassOutput().getPath("plugin", "less4j", "bootstrap", "assets", "bootstrap.css");
    assertNotNull(f);
    assertTrue(f.exists());
  }
}
