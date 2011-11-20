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

import org.juzu.Application;
import org.juzu.impl.compiler.*;
import org.juzu.impl.compiler.Compiler;
import org.juzu.impl.processor.ModelProcessor;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.impl.utils.Content;
import org.juzu.test.AbstractTestCase;

import java.io.ObjectInputStream;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PathTestCase extends AbstractTestCase
{

   public void testPathChangeValue() throws Exception
   {
      DiskFileSystem fs = diskFS("metamodel", "template", "pathannotation");

      //
      RAMFileSystem sourcePath = new RAMFileSystem();
      ReadFileSystem.copy(fs, sourcePath);
      RAMFileSystem sourceOutput = new RAMFileSystem();
      RAMFileSystem classOutput = new RAMFileSystem();

      //
      Compiler.Builder builder = Compiler.Builder.create().sourcePath(sourcePath).sourceOutput(sourceOutput).classOutput(classOutput).addClassPath(Application.class);
      Compiler compiler = builder.build();
      compiler.addAnnotationProcessor(new ModelProcessor());
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());

      //
      Content content = sourceOutput.getPath("org", "juzu", "model2.ser").getContent();
      ObjectInputStream ois = new ObjectInputStream(content.getInputStream());
      MetaModel mm = (MetaModel)ois.readObject();

      //
      MetaModel expected = new MetaModel();
      ApplicationMetaModel b = expected.addApplication("metamodel.template.pathannotation", "PathannotationApplication");
      TemplateMetaModel c = b.addTemplate("foo.gtmpl");
      c.addRef(expected.addTemplateRef("metamodel.template.pathannotation.A", "index", "foo.gtmpl"));
      assertEquals(expected.toJSON(), mm.toJSON());

      //
      ReadFileSystem.copy(fs, sourcePath);
      sourcePath.getPath("metamodel", "template", "pathannotation", "package-info.java").del();
      RAMPath a = sourcePath.getPath("metamodel", "template", "pathannotation", "A.java");
      a.update(a.getContent().getCharSequence().toString().replace("foo.gtmpl", "bar.gtmpl"));
      classOutput.getPath("metamodel", "template", "pathannotation", "A.class").del();

      //
      builder = builder.addClassPath(classOutput);
      compiler = builder.build();
      compiler.addAnnotationProcessor(new ModelProcessor());
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());

      //
      content = sourceOutput.getPath("org", "juzu", "model2.ser").getContent();
      ois = new ObjectInputStream(content.getInputStream());
      mm = (MetaModel)ois.readObject();

      //
      expected = new MetaModel();
      b = expected.addApplication("metamodel.template.pathannotation", "PathannotationApplication");
      c = b.addTemplate("bar.gtmpl");
      c.addRef(expected.addTemplateRef("metamodel.template.pathannotation.A", "index", "bar.gtmpl"));
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void testPathRemoveAnnotation() throws Exception
   {
      DiskFileSystem fs = diskFS("metamodel", "template", "pathannotation");

      //
      RAMFileSystem sourcePath = new RAMFileSystem();
      ReadFileSystem.copy(fs, sourcePath);
      RAMFileSystem sourceOutput = new RAMFileSystem();
      RAMFileSystem classOutput = new RAMFileSystem();

      //
      Compiler.Builder builder = Compiler.Builder.create().sourcePath(sourcePath).sourceOutput(sourceOutput).classOutput(classOutput).addClassPath(Application.class);
      Compiler compiler = builder.build();
      compiler.addAnnotationProcessor(new ModelProcessor());
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());

      //
      Content content = sourceOutput.getPath("org", "juzu", "model2.ser").getContent();
      ObjectInputStream ois = new ObjectInputStream(content.getInputStream());
      MetaModel mm = (MetaModel)ois.readObject();

      //
      MetaModel expected = new MetaModel();
      ApplicationMetaModel b = expected.addApplication("metamodel.template.pathannotation", "PathannotationApplication");
      TemplateMetaModel c = b.addTemplate("foo.gtmpl");
      c.addRef(expected.addTemplateRef("metamodel.template.pathannotation.A", "index", "foo.gtmpl"));
      assertEquals(expected.toJSON(), mm.toJSON());

      //
      ReadFileSystem.copy(fs, sourcePath);
      sourcePath.getPath("metamodel", "template", "pathannotation", "package-info.java").del();
      RAMPath a = sourcePath.getPath("metamodel", "template", "pathannotation", "A.java");
      a.update(a.getContent().getCharSequence().toString().replace("@Path(\"foo.gtmpl\")", ""));
      classOutput.getPath("metamodel", "template", "pathannotation", "A.class").del();

      //
      builder = builder.addClassPath(classOutput);
      compiler = builder.build();
      compiler.addAnnotationProcessor(new ModelProcessor());
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());

      //
      content = sourceOutput.getPath("org", "juzu", "model2.ser").getContent();
      ois = new ObjectInputStream(content.getInputStream());
      mm = (MetaModel)ois.readObject();

      //
      expected = new MetaModel();
      expected.addApplication("metamodel.template.pathannotation", "PathannotationApplication");
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void testPathRemoveApplication() throws Exception
   {
      DiskFileSystem fs = diskFS("metamodel", "template", "pathannotation");

      //
      RAMFileSystem sourcePath = new RAMFileSystem();
      ReadFileSystem.copy(fs, sourcePath);
      RAMFileSystem sourceOutput = new RAMFileSystem();
      RAMFileSystem classOutput = new RAMFileSystem();

      //
      Compiler.Builder builder = Compiler.Builder.create().sourcePath(sourcePath).sourceOutput(sourceOutput).classOutput(classOutput).addClassPath(Application.class);
      Compiler compiler = builder.build();
      compiler.addAnnotationProcessor(new ModelProcessor());
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());

      //
      Content content = sourceOutput.getPath("org", "juzu", "model2.ser").getContent();
      ObjectInputStream ois = new ObjectInputStream(content.getInputStream());
      MetaModel mm = (MetaModel)ois.readObject();

      //
      MetaModel expected = new MetaModel();
      ApplicationMetaModel b = expected.addApplication("metamodel.template.pathannotation", "PathannotationApplication");
      TemplateMetaModel c = b.addTemplate("foo.gtmpl");
      c.addRef(expected.addTemplateRef("metamodel.template.pathannotation.A", "index", "foo.gtmpl"));
      assertEquals(expected.toJSON(), mm.toJSON());

      //
      ReadFileSystem.copy(fs, sourcePath);
      sourcePath.getPath("metamodel", "template", "pathannotation", "package-info.java").del();
      sourcePath.getPath("metamodel", "template", "pathannotation", "A.java").del();
      classOutput.getPath("metamodel", "template", "pathannotation", "package-info.class").del();
      classOutput.getPath("metamodel", "template", "pathannotation", "B.class").del();

      //
      builder = builder.addClassPath(classOutput);
      compiler = builder.build();
      compiler.addAnnotationProcessor(new ModelProcessor());
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());

      //
      content = sourceOutput.getPath("org", "juzu", "model2.ser").getContent();
      ois = new ObjectInputStream(content.getInputStream());
      mm = (MetaModel)ois.readObject();

      //
      expected = new MetaModel();
      expected.addTemplateRef("metamodel.template.pathannotation.A", "index", "foo.gtmpl");
      assertEquals(expected.toJSON(), mm.toJSON());
   }
}
