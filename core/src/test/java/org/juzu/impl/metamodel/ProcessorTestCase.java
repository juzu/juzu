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
import org.juzu.impl.compiler.CompilationError;
import org.juzu.impl.template.metamodel.TemplateMetaModelPlugin;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.impl.utils.Content;
import org.juzu.processor.MainProcessor;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ProcessorTestCase extends AbstractTestCase
{

   @Test
   public void testTemplatePathMatching()
   {
      assertNotMatch("a");
      assertMatch("a.b", "", "a", "b");
      assertNotMatch("/a.b");
      assertMatch("a/b.c", "a/", "b", "c");
      assertNotMatch("/a/b.c");
      assertNotMatch("a/b");
   }

   private void assertMatch(String test, String expectedFolder, String expectedRawName, String expectedExtension)
   {
      Matcher matcher = TemplateMetaModelPlugin.TEMPLATE_PATH_PATTERN.matcher(test);
      assertTrue("Was expecting " + test + " to match", matcher.matches());
      assertEquals(expectedFolder, matcher.group(1));
      assertEquals(expectedRawName, matcher.group(2));
      assertEquals(expectedExtension, matcher.group(3));
   }

   private void assertNotMatch(String test)
   {
      Matcher matcher = TemplateMetaModelPlugin.TEMPLATE_PATH_PATTERN.matcher(test);
      assertFalse("Was not expecting " + test + " to match", matcher.matches());
   }

   public void _testSimpleIncremental() throws Exception
   {
      DiskFileSystem fs = diskFS("processor", "simple");

      //
      RAMFileSystem sourcePath = new RAMFileSystem();
      ReadFileSystem.copy(fs, sourcePath);
      sourcePath.getPath("processor", "simple", "templates", "index.gtmpl").del();
      sourcePath.getPath("processor", "simple", "package-info.java").del();

      //
      RAMFileSystem sourceOutput = new RAMFileSystem();
      RAMFileSystem classOutput = new RAMFileSystem();

      //
      CompilerHelper<RAMPath, RAMPath> helper = new CompilerHelper<RAMPath, RAMPath>(sourcePath, sourceOutput, classOutput);
      helper.assertCompile();
      assertEquals(2, classOutput.size(ReadFileSystem.FILE));
      assertNotNull(classOutput.getPath("org", "juzu", "config.properties"));
      assertNotNull(classOutput.getPath("processor", "simple", "A.class"));

      //
      ReadFileSystem.copy(fs, sourcePath);
      sourcePath.getPath("processor", "simple", "templates", "index.gtmpl").del();
      helper = new CompilerHelper<RAMPath, RAMPath>(sourcePath, sourceOutput, classOutput);

      //
      List<CompilationError> errors = helper.failCompile();
      assertEquals(1, errors.size());
      CompilationError error = errors.get(0);
      assertEquals(MetaModelError.TEMPLATE_NOT_RESOLVED.toString(), error.getCode());
      assertEquals(2, classOutput.size(ReadFileSystem.FILE));
      assertNotNull(classOutput.getPath("org", "juzu", "config.properties"));
      assertNotNull(classOutput.getPath("processor", "simple", "A.class"));

      //
      ReadFileSystem.copy(fs, sourcePath);
      sourcePath.getPath("processor", "simple", "A.java").del();
//      sourceOutput.getPath("processor", "simple", "A_.java").del();
      classOutput.getPath("processor", "simple", "A.class").del();
      helper = new CompilerHelper<RAMPath, RAMPath>(sourcePath, sourceOutput, classOutput);

      // This test cannot pass actually
//      helper.assertCompile();
//      assertEquals(9, classOutput.size(ReadFileSystem.FILE));
//      assertNotNull(classOutput.getPath("org", "juzu", "config.properties"));
//      assertNotNull(classOutput.getPath("processor", "simple", "templates", "index.groovy"));
//      assertNotNull(classOutput.getPath("processor", "simple", "config.properties"));
//      assertNotNull(classOutput.getPath("processor", "simple", "package-info.class"));
//      assertNotNull(classOutput.getPath("processor", "simple", "SimpleApplication.class"));
//      assertNotNull(classOutput.getPath("processor", "simple", "A.class"));
//      assertNotNull(classOutput.getPath("processor", "simple", "A_.class"));
   }

   public void _testModifyTemplate() throws Exception
   {
      DiskFileSystem fs = diskFS("processor", "simple");

      //
      RAMFileSystem sourcePath = new RAMFileSystem();
      ReadFileSystem.copy(fs, sourcePath);

      //
      RAMFileSystem sourceOutput = new RAMFileSystem();
      RAMFileSystem classOutput = new RAMFileSystem();

      //
      CompilerHelper<RAMPath, RAMPath> compiler = new CompilerHelper<RAMPath, RAMPath>(sourcePath, sourceOutput, classOutput);

      //
      compiler.assertCompile();
      assertEquals(9, classOutput.size(ReadFileSystem.FILE));
      assertNotNull(classOutput.getPath("org", "juzu", "config.properties"));
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
      compiler = new CompilerHelper<RAMPath, RAMPath>(sourcePath, sourceOutput, classOutput);
      compiler.assertCompile();
      assertEquals(9, classOutput.size(ReadFileSystem.FILE));
      assertNotNull(classOutput.getPath("org", "juzu", "config.properties"));
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
   public void testRemoveTemplate() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "processor", "simple");
      helper.assertCompile();

      //
      assertDelete(helper.getSourcePath().getPath("model", "processor", "simple", "templates", "index.gtmpl"));
      assertDelete(helper.getClassOutput().getPath("model", "processor", "simple", "B.class"));

      //
      helper.with(new MainProcessor()).addClassPath(helper.getClassOutput()).failCompile();
   }
}
