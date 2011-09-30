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
import org.juzu.impl.classloading.RAMClassLoader;
import org.juzu.impl.compiler.FileKey;
import org.juzu.impl.compiler.CompilerContext;
import org.juzu.impl.spi.fs.ram.RAMDir;
import org.juzu.impl.spi.fs.ram.RAMFile;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.impl.spi.template.TemplateStub;
import org.juzu.impl.utils.Content;
import org.juzu.text.WriterPrinter;

import javax.tools.JavaFileObject;
import java.io.StringWriter;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateProcessorTestCase extends TestCase
{

   public void testFoo() throws Exception
   {

      RAMFileSystem ramFS = new RAMFileSystem();
      RAMDir root = ramFS.getRoot();
      RAMDir foo = root.addDir("foo");
      RAMFile a = foo.addFile("A.java").update("package foo; public class A { @org.juzu.template.Template(\"B.gtmpl\") org.juzu.template.TemplateRenderer template; }");
      RAMFile b = foo.addFile("B.gtmpl").update("<% out.print('hello') %>");

      //
      final CompilerContext<RAMPath, RAMDir, RAMFile> compiler = new CompilerContext<RAMPath, RAMDir, RAMFile>(ramFS);
      compiler.addAnnotationProcessor(new TemplateProcessor());
      assertTrue(compiler.compile());

      //
      Content<?> content = compiler.getClassOutput(FileKey.newResourceName("foo", "B.groovy"));
      assertNotNull(content);
      assertEquals(3, compiler.getClassOutputKeys().size());

      //
      assertEquals(1, compiler.getSourceOutputKeys().size());
      Content<?> content2 = compiler.getSourceOutput(FileKey.newJavaName("foo.B", JavaFileObject.Kind.SOURCE));
      assertNotNull(content2);

      //
      ClassLoader cl = new RAMClassLoader(Thread.currentThread().getContextClassLoader(), compiler.getClassOutput());

      Class<?> aClass = cl.loadClass("foo.A");
      Class<?> bClass = cl.loadClass("foo.B");
      TemplateStub template = (TemplateStub)bClass.newInstance();
      StringWriter out = new StringWriter();
      template.render(new WriterPrinter(out), null, null);
      assertEquals("hello", out.toString());
   }

}
