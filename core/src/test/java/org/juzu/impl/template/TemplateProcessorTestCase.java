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

package org.juzu.impl.template;

import junit.framework.TestCase;
import org.juzu.impl.application.JuzuProcessor;
import org.juzu.impl.compiler.CompilationError;
import org.juzu.impl.compiler.FileKey;
import org.juzu.impl.compiler.Compiler;
import org.juzu.impl.spi.fs.ram.RAMDir;
import org.juzu.impl.spi.fs.ram.RAMFile;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.impl.spi.template.TemplateStub;
import org.juzu.impl.utils.Content;
import org.juzu.template.TemplateRenderContext;
import org.juzu.text.WriterPrinter;

import javax.tools.JavaFileObject;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateProcessorTestCase extends TestCase
{

   public void testFoo() throws Exception
   {

      RAMFileSystem ramFS = new RAMFileSystem();
      RAMDir root = ramFS.getRoot();
      RAMDir bar = root.addDir("bar");
      RAMDir templates = bar.addDir("templates");
      RAMFile barPkgInfo = bar.addFile("package-info.java").update(
         "@Application\n" +
         "package bar;\n" +
         "import org.juzu.Application;"
      );
      RAMDir foo = bar.addDir("foo");
      RAMFile a = foo.addFile("A.java").update("package bar.foo; public class A { @org.juzu.Path(\"B.gtmpl\") org.juzu.template.Template template; }");
      RAMFile b = templates.addFile("B.gtmpl").update("<% out.print('hello') %>");

      //
      RAMFileSystem output = new RAMFileSystem();
      final Compiler<RAMPath, ?> compiler = new Compiler<RAMPath, RAMPath>(ramFS, output);
      compiler.addAnnotationProcessor(new JuzuProcessor());
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());

      //
      Content<?> content = compiler.getClassOutput(FileKey.newResourceName("bar.templates", "B.groovy"));
      assertNotNull(content);
      assertTrue(compiler.getClassOutputKeys().size() > 0);

      //
      assertTrue(compiler.getSourceOutputKeys().size() > 1);
      Content<?> content2 = compiler.getSourceOutput(FileKey.newJavaName("bar.templates.B", JavaFileObject.Kind.SOURCE));
      assertNotNull(content2);

      //
      ClassLoader cl = new URLClassLoader(new URL[]{output.getURL()}, Thread.currentThread().getContextClassLoader());

      Class<?> aClass = cl.loadClass("bar.foo.A");
      Class<?> bClass = cl.loadClass("bar.templates.B");
      TemplateStub template = (TemplateStub)bClass.newInstance();
      StringWriter out = new StringWriter();
      template.render(new TemplateRenderContext(new WriterPrinter(out)));
      assertEquals("hello", out.toString());
   }

}
