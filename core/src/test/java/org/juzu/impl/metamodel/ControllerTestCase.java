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
import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.application.metamodel.ApplicationsMetaModel;
import org.juzu.impl.controller.metamodel.ControllerMetaModel;
import org.juzu.impl.controller.metamodel.ControllerMetaModelPlugin;
import org.juzu.impl.template.metamodel.TemplateMetaModelPlugin;
import org.juzu.impl.utils.JSON;
import org.juzu.processor.MainProcessor;
import org.juzu.request.Phase;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.Tools;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerTestCase extends AbstractTestCase
{

   @Test
   public void testBuild() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller").with(new MainProcessor());
      helper.assertCompile();

      //
      MetaModel mm = Tools.unserialize(MetaModel.class, helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser"));

      //
      JSON expected = new JSON()
         .list("applications", new JSON().
            list("controllers", new JSON().
               set("handle", "ElementHandle.Class[fqn=model.meta.controller.A]").
               list("methods", new JSON().
                  set("handle", "ElementHandle.Method[fqn=model.meta.controller.A,name=index,parameterTypes[]]").
                  set("id", null).
                  set("name", "index").
                  list("parameters").
                  set("phase", "RENDER")
               )
            ).
            set("fqn", "model.meta.controller.ControllerApplication").
            set("handle", "ElementHandle.Package[qn=model.meta.controller]").
            list("templates")
         );
      assertEquals(expected, mm.toJSON());

      //
      List<MetaModelEvent> events = mm.getQueue().clear();
      ApplicationMetaModel application = mm.getChild(ApplicationsMetaModel.KEY).iterator().next();
      ControllerMetaModel controller = application.getControllers().iterator().next();
      assertEquals(Arrays.asList(MetaModelEvent.createAdded(application), MetaModelEvent.createAdded(controller)), events);
   }

   public void _testRemoveApplication() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller").with(new MainProcessor());
      helper.assertCompile();
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      assertDelete(helper.getSourcePath().getPath("model", "meta", "controller", "A.java"));
      assertDelete(helper.getSourcePath().getPath("model", "meta", "controller", "package-info.java"));
      assertDelete(helper.getClassOutput().getPath("model", "meta", "controller", "package-info.class"));

      //
      helper.with(new MainProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, ser);

      //
      MetaModel expected = new MetaModel();
//      expected.addController("model.meta.controller.A").addMethod(Phase.RENDER, "index", Collections.<Map.Entry<String, String>>emptyList());
      assertEquals(expected.toJSON(), mm.toJSON());

      //
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(2, events.size());
      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
      assertInstanceOf(ApplicationMetaModel.class, events.get(0).getObject());
      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(1).getType());
      assertInstanceOf(ControllerMetaModel.class, events.get(1).getObject());
   }

   public void _testChangeAnnotation() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller").with(new MainProcessor());
      helper.assertCompile();
      File ser = helper.getSourceOutput().getPath("org", "juzu", "model2.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      File a = helper.getSourcePath().getPath("model", "meta", "controller", "A.java");
      Tools.write(Tools.read(a).replace("View", "Action"), a);
      assertDelete(helper.getSourcePath().getPath("model", "meta", "controller", "package-info.java"));

      //
      helper.with(new MainProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, ser);

      //
      MetaModel expected = new MetaModel();
      ApplicationMetaModel application = expected.addApplication("model.meta.controller", "ControllerApplication");
      ControllerMetaModel controller = application.addController("model.meta.controller.A");
      controller.addMethod(Phase.ACTION, "index", Collections.<Map.Entry<String, String>>emptyList());
      application.getControllers().add(controller);
      assertEquals(expected.toJSON(), mm.toJSON());

      //
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(1, events.size());
      assertEquals(MetaModelEvent.UPDATED, events.get(0).getType());
      assertTrue(events.get(0).getObject() instanceof ControllerMetaModel);
   }

   public void _testRemoveAnnotation() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller").with(new MainProcessor());
      helper.assertCompile();
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      File a = helper.getSourcePath().getPath("model", "meta", "controller", "A.java");
      Tools.write(Tools.read(a).replace("@View", ""), a);
      assertDelete(helper.getClassOutput().getPath("model", "meta", "controller", "A.class"));
      assertDelete(helper.getSourcePath().getPath("model", "meta", "controller", "package-info.java"));

      //
      helper.with(new MainProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, ser);

      //
      MetaModel expected = new MetaModel();
      expected.addApplication("model.meta.controller", "ControllerApplication");
      assertEquals(expected.toJSON(), mm.toJSON());

      //
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(1, events.size());
      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
      assertTrue(events.get(0).getObject() instanceof ControllerMetaModel);
   }

   public void _testRemoveSingleMethod() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller").with(new MainProcessor());
      helper.assertCompile();
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      File a = helper.getSourcePath().getPath("model", "meta", "controller", "A.java");
      Tools.write(Tools.read(a).replace("@View public void index() { }", ""), a);
      assertDelete(helper.getClassOutput().getPath("model", "meta", "controller", "A.class"));
      assertDelete(helper.getSourcePath().getPath("model", "meta", "controller", "package-info.java"));

      //
      helper.with(new MainProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, ser);
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(1, events.size());
      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
      assertTrue(events.get(0).getObject() instanceof ControllerMetaModel);

      //
      MetaModel expected = new MetaModel();
      expected.addApplication("model.meta.controller", "ControllerApplication");
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void _testRemoveMethod() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller").with(new MainProcessor());
      File a = helper.getSourcePath().getPath("model", "meta", "controller", "A.java");
      String src = Tools.read(a);
      Tools.write(src.replace("@View", "\n@View public void show() { }\n@View"), a);
      helper.assertCompile();

      //
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      Tools.write(src, a);
      assertDelete(helper.getClassOutput().getPath("model", "meta", "controller", "A.class"));
      assertDelete(helper.getSourcePath().getPath("model", "meta", "controller", "package-info.java"));

      //
      helper.with(new MainProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, ser);
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(1, events.size());
      assertEquals(MetaModelEvent.UPDATED, events.get(0).getType());
      assertTrue(events.get(0).getObject() instanceof ControllerMetaModel);

      //
      MetaModel expected = new MetaModel();
      ApplicationMetaModel application = expected.addApplication("model.meta.controller", "ControllerApplication");
      ControllerMetaModel controller = application.addController("model.meta.controller.A");
      controller.addMethod(Phase.RENDER, "index", Collections.<String, String>emptyMap().entrySet());
      application.getControllers().add(controller);
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void _testRemoveOverloadedMethod() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller").with(new MainProcessor());
      File a = helper.getSourcePath().getPath("model", "meta", "controller", "A.java");
      String src = Tools.read(a);
      Tools.write(src.replace("@View", "\n@View public void index(String s) { }\n@View"), a);
      helper.assertCompile();

      //
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      Tools.write(src, a);
      assertDelete(helper.getClassOutput().getPath("model", "meta", "controller", "A.class"));
      assertDelete(helper.getSourcePath().getPath("model", "meta", "controller", "package-info.java"));

      //
      helper.with(new MainProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, ser);
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(1, events.size());
      assertEquals(MetaModelEvent.UPDATED, events.get(0).getType());
      assertTrue(events.get(0).getObject() instanceof ControllerMetaModel);

      //
      MetaModel expected = new MetaModel();
      ApplicationMetaModel application = expected.addApplication("model.meta.controller", "ControllerApplication");
      ControllerMetaModel controller = application.addController("model.meta.controller.A");
      controller.addMethod(Phase.RENDER, "index", Collections.<String, String>emptyMap().entrySet());
      application.getControllers().add(controller);
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void _testRemoveMethods() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller").with(new MainProcessor());
      File a = helper.getSourcePath().getPath("model", "meta", "controller", "A.java");
      String src = Tools.read(a);
      Tools.write(src.replace("@View", "\n@View public void show() { }\n@View"), a);
      helper.assertCompile();

      //
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      Tools.write(src.replace("@View public void index() { }", ""), a);
      assertDelete(helper.getClassOutput().getPath("model", "meta", "controller", "A.class"));
      assertDelete(helper.getSourcePath().getPath("model", "meta", "controller", "package-info.java"));

      //
      helper.with(new MainProcessor()).addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, ser);
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(1, events.size());
      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
      assertTrue(events.get(0).getObject() instanceof ControllerMetaModel);

      //
      MetaModel expected = new MetaModel();
      expected.addApplication("model.meta.controller", "ControllerApplication");
      assertEquals(expected.toJSON(), mm.toJSON());
   }

   public void _testRefactorPackageName() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller").with(new MainProcessor());
      helper.assertCompile();

      //
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      File a = helper.getSourcePath().getPath("model", "meta", "controller", "A.java");
      File sub = new File(a.getParentFile(), "sub");
      assertTrue(sub.mkdir());
      File tmp = new File(sub, a.getName());
      assertTrue(a.renameTo(tmp));
      a = tmp;
      Tools.write(Tools.read(a).replace("package model.meta.controller;", "package model.meta.controller.sub;"), a);

      //
      assertDelete(helper.getClassOutput().getPath("model", "meta", "controller", "A.class"));
      assertDelete(helper.getSourcePath().getPath("model", "meta", "controller", "package-info.java"));

      //
      helper.with(new MainProcessor()).addClassPath(helper.getClassOutput()).assertCompile();

      //
      mm = Tools.unserialize(MetaModel.class, ser);
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(2, events.size());
      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
      assertEquals(ElementHandle.Class.create(new FQN("model.meta.controller.A")), ((ControllerMetaModel)events.get(0).getObject()).getHandle());
      assertEquals(MetaModelEvent.AFTER_ADD, events.get(1).getType());
      assertEquals(ElementHandle.Class.create(new FQN("model.meta.controller.sub.A")), ((ControllerMetaModel)events.get(1).getObject()).getHandle());
   }
}
