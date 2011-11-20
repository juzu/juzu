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
import org.juzu.Phase;
import org.juzu.impl.compiler.*;
import org.juzu.impl.compiler.Compiler;
import org.juzu.impl.processor.ModelProcessor;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.impl.spi.fs.ram.RAMFile;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.utils.Content;
import org.juzu.test.AbstractTestCase;

import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerTestCase extends AbstractTestCase
{

   public void testBuild() throws Exception
   {
      DiskFileSystem fs = diskFS("metamodel", "controller", "simple");

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
      ApplicationMetaModel amm = expected.addApplication("metamodel.controller.simple", "SimpleApplication");
      ControllerMetaModel c = expected.addController("metamodel.controller.simple.A");
      c.addMethod(Phase.RENDER, "index", Collections.<Map.Entry<String, String>>emptyList());
      amm.addController(c);
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void testRemoveApplication() throws Exception
   {
      DiskFileSystem fs = diskFS("metamodel", "controller", "simple");

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

      // We remove the application declaration
      ReadFileSystem.copy(fs, sourcePath);
      sourcePath.getPath("metamodel", "controller", "simple", "A.java").del();
      sourcePath.getPath("metamodel", "controller", "simple", "package-info.java").del();
      classOutput.getPath("metamodel", "controller", "simple", "package-info.class").del();

      //
      builder.addClassPath(classOutput);
      compiler = builder.build();
      compiler.addAnnotationProcessor(new ModelProcessor());
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());

      //
      Content content = sourceOutput.getPath("org", "juzu", "model2.ser").getContent();
      ObjectInputStream ois = new ObjectInputStream(content.getInputStream());
      MetaModel mm = (MetaModel)ois.readObject();

      //
      MetaModel expected = new MetaModel();
      ControllerMetaModel c = expected.addController("metamodel.controller.simple.A");
      c.addMethod(Phase.RENDER, "index", Collections.<Map.Entry<String, String>>emptyList());
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void testRemoveAnnotation() throws Exception
   {
      DiskFileSystem fs = diskFS("metamodel", "controller", "simple");

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
      ReadFileSystem.copy(fs, sourcePath);
      RAMFile a = (RAMFile)sourcePath.getPath("metamodel", "controller", "simple", "A.java");
      a.update(a.getContent().getCharSequence().toString().replace("@View", ""));
      sourcePath.getPath("metamodel", "controller", "simple", "package-info.java").del();

      //
      builder.addClassPath(classOutput);
      compiler = builder.build();
      compiler.addAnnotationProcessor(new ModelProcessor());
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());

      //
      Content content = sourceOutput.getPath("org", "juzu", "model2.ser").getContent();
      ObjectInputStream ois = new ObjectInputStream(content.getInputStream());
      MetaModel mm = (MetaModel)ois.readObject();

      //
      MetaModel expected = new MetaModel();
      expected.addApplication("metamodel.controller.simple", "SimpleApplication");
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void testRemoveControllerMethod() throws Exception
   {
      DiskFileSystem fs = diskFS("metamodel", "controller", "simple");

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
      ReadFileSystem.copy(fs, sourcePath);
      RAMFile a = (RAMFile)sourcePath.getPath("metamodel", "controller", "simple", "A.java");
      a.update(a.getContent().getCharSequence().toString().replace("@View\n   public void index()\n   {\n   }", ""));
      sourcePath.getPath("metamodel", "controller", "simple", "package-info.java").del();

      //
      builder.addClassPath(classOutput);
      compiler = builder.build();
      compiler.addAnnotationProcessor(new ModelProcessor());
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());

      //
      Content content = sourceOutput.getPath("org", "juzu", "model2.ser").getContent();
      ObjectInputStream ois = new ObjectInputStream(content.getInputStream());
      MetaModel mm = (MetaModel)ois.readObject();

      //
      MetaModel expected = new MetaModel();
      expected.addApplication("metamodel.controller.simple", "SimpleApplication");
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void testChangeAnnotation() throws Exception
   {
      DiskFileSystem fs = diskFS("metamodel", "controller", "simple");

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
      ReadFileSystem.copy(fs, sourcePath);
      RAMFile a = (RAMFile)sourcePath.getPath("metamodel", "controller", "simple", "A.java");
      a.update(a.getContent().getCharSequence().toString().replace("View", "Action"));
      sourcePath.getPath("metamodel", "controller", "simple", "package-info.java").del();

      //
      builder.addClassPath(classOutput);
      compiler = builder.build();
      compiler.addAnnotationProcessor(new ModelProcessor());
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());

      //
      Content content = sourceOutput.getPath("org", "juzu", "model2.ser").getContent();
      ObjectInputStream ois = new ObjectInputStream(content.getInputStream());
      MetaModel mm = (MetaModel)ois.readObject();

      //
      MetaModel expected = new MetaModel();
      ApplicationMetaModel amm = expected.addApplication("metamodel.controller.simple", "SimpleApplication");
      ControllerMetaModel c = expected.addController("metamodel.controller.simple.A");
      c.addMethod(Phase.ACTION, "index", Collections.<Map.Entry<String, String>>emptyList());
      amm.addController(c);
      assertEquals(expected.toJSON(), mm.toJSON());
   }
}
