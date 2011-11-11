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

package org.juzu.impl.processor;

import junit.framework.TestCase;
import org.juzu.impl.compiler.*;
import org.juzu.impl.compiler.Compiler;
import org.juzu.impl.spi.fs.ram.RAMDir;
import org.juzu.impl.spi.fs.ram.RAMFile;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.impl.utils.Tools;

import java.util.Collections;
import java.util.Properties;
import java.util.regex.Matcher;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MainProcessorTestCase extends TestCase
{

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
      Matcher matcher = Foo.NAME_PATTERN.matcher(test);
      assertTrue("Was expecting " + test + " to match", matcher.matches());
      assertEquals(expectedFolder, matcher.group(1));
      assertEquals(expectedRawName, matcher.group(2));
      assertEquals(expectedExtension, matcher.group(3));
   }

   private void assertNotMatch(String test)
   {
      Matcher matcher = Foo.NAME_PATTERN.matcher(test);
      assertFalse("Was not expecting " + test + " to match", matcher.matches());
   }

   public void testIncremental() throws Exception
   {
      RAMFileSystem sourcePath = new RAMFileSystem();
      RAMPath incremental = sourcePath.getRoot().addDir("compilation").addDir("incremental");
      RAMPath a = incremental.addFile("A.java");
      a.update("package compilation.incremental; public class A { @javax.inject.Inject @org.juzu.Path(\"index.gtmpl\") org.juzu.template.Template template;\n @org.juzu.View public void index() { } }");
      RAMPath index = incremental.addDir("templates").addFile("index.gtmpl");

      //
      RAMFileSystem sourceOutput = new RAMFileSystem();
      RAMFileSystem classOutput = new RAMFileSystem();

      //
      Compiler<RAMPath, RAMPath> compiler = new Compiler<RAMPath, RAMPath>(sourcePath, sourceOutput, classOutput);
      compiler.addAnnotationProcessor(new MainProcessor());
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());

      //
/*
      System.out.println("Source:");
      sourceOutput.dump(System.out);
      System.out.println("Class:");
      classOutput.dump(System.out);
*/

      // Update
      a.del();
      RAMPath packageInfo = incremental.addFile("package-info.java");
      packageInfo.update("@org.juzu.Application package compilation.incremental;");

      //
      compiler = new Compiler<RAMPath, RAMPath>(sourcePath, classOutput, sourceOutput,classOutput);
      compiler.addAnnotationProcessor(new MainProcessor());
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());

/*
      System.out.println("Source:");
      sourceOutput.dump(System.out);
      System.out.println("Class:");
      classOutput.dump(System.out);
*/

      //
      RAMFile config = (RAMFile)((RAMDir)((RAMDir)classOutput.getRoot().getChild("compilation")).getChild("incremental")).getChild("config.properties");
      Properties props = new Properties();
      props.load(config.getContent().getInputStream());
      assertEquals(2, props.size());
      assertEquals(Tools.<Object>set("compilation.incremental.A_", "compilation.incremental.templates.index_"), props.keySet());
      assertEquals("controller", props.get("compilation.incremental.A_"));
      assertEquals("template", props.get("compilation.incremental.templates.index_"));
   }

   public void testIncremental2() throws Exception
   {
      RAMFileSystem sourcePath = new RAMFileSystem();
      RAMPath incremental = sourcePath.getRoot().addDir("compilation").addDir("incremental");
      RAMPath a = incremental.addFile("A.java");
      a.update("package compilation.incremental; public class A { @javax.inject.Inject @org.juzu.Path(\"index.gtmpl\") org.juzu.template.Template template;\n @org.juzu.View public void index() { } }");

      //
      RAMFileSystem sourceOutput = new RAMFileSystem();
      RAMFileSystem classOutput = new RAMFileSystem();

      //
      Compiler<RAMPath, RAMPath> compiler = new Compiler<RAMPath, RAMPath>(sourcePath, sourceOutput, classOutput);
      compiler.addAnnotationProcessor(new MainProcessor());
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());

      //
/*
      System.out.println("Source:");
      sourceOutput.dump(System.out);
      System.out.println("Class:");
      classOutput.dump(System.out);
*/

      // Update
      a.del();
      RAMPath packageInfo = incremental.addFile("package-info.java");
      packageInfo.update("@org.juzu.Application package compilation.incremental;");

      //
      compiler = new Compiler<RAMPath, RAMPath>(sourcePath, classOutput, sourceOutput,classOutput);
      compiler.addAnnotationProcessor(new MainProcessor());
      assertEquals(1, compiler.compile().size());

      //
/*
      System.out.println("Source:");
      sourceOutput.dump(System.out);
      System.out.println("Class:");
      classOutput.dump(System.out);
*/

      //
      RAMPath index = incremental.addDir("templates").addFile("index.gtmpl");
      compiler = new Compiler<RAMPath, RAMPath>(sourcePath, classOutput, sourceOutput,classOutput);
      compiler.addAnnotationProcessor(new MainProcessor());
      assertEquals(0, compiler.compile().size());

      //
      RAMFile config = (RAMFile)((RAMDir)((RAMDir)classOutput.getRoot().getChild("compilation")).getChild("incremental")).getChild("config.properties");
      Properties props = new Properties();
      props.load(config.getContent().getInputStream());
      assertEquals(2, props.size());
      assertEquals(Tools.<Object>set("compilation.incremental.A_", "compilation.incremental.templates.index_"), props.keySet());
      assertEquals("controller", props.get("compilation.incremental.A_"));
      assertEquals("template", props.get("compilation.incremental.templates.index_"));
   }
}
