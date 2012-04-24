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

import japa.parser.ASTHelper;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import org.junit.Test;
import org.juzu.Action;
import org.juzu.View;
import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.application.metamodel.ApplicationsMetaModel;
import org.juzu.impl.controller.metamodel.ControllerMetaModel;
import org.juzu.impl.utils.JSON;
import org.juzu.impl.utils.QN;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.Tools;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;
import org.juzu.test.JavaFile;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerTestCase extends AbstractTestCase
{

   @Test
   public void testBuild() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller");
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

   @Test
   public void testRemoveApplication() throws Exception {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller");
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
      helper.addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, ser);

      //
      MetaModel expected = new MetaModel();
      assertEquals(expected.toJSON(), mm.toJSON());

      //
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(2, events.size());
      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
      assertInstanceOf(ApplicationMetaModel.class, events.get(0).getObject());
      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(1).getType());
      assertInstanceOf(ControllerMetaModel.class, events.get(1).getObject());
   }

   @Test
   public void testChangeAnnotation() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller");
      helper.assertCompile();
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      JavaFile file = helper.assertJavaFile("model", "meta", "controller", "A.java");
      ClassOrInterfaceDeclaration a = file.assertDeclaration();
      MethodDeclaration decl = (MethodDeclaration)a.getMembers().get(0);
      decl.getAnnotations().get(0).setName(ASTHelper.createNameExpr(Action.class.getName()));
      helper.saveJavaFile(file);
      assertDelete(helper.getSourcePath().getPath("model", "meta", "controller", "package-info.java"));

      //
      helper.addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, ser);

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
                  set("phase", "ACTION")
               )
            ).
            set("fqn", "model.meta.controller.ControllerApplication").
            set("handle", "ElementHandle.Package[qn=model.meta.controller]").
            list("templates")
         );
      assertEquals(expected, mm.toJSON());

      //
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(2, events.size());
      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
      assertTrue(events.get(0).getObject() instanceof ControllerMetaModel);
      assertEquals(MetaModelEvent.AFTER_ADD, events.get(1).getType());
      assertTrue(events.get(1).getObject() instanceof ControllerMetaModel);
   }

   @Test
   public void testRemoveAnnotation() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller");
      helper.assertCompile();
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      JavaFile file = helper.assertJavaFile("model", "meta", "controller", "A.java");
      ClassOrInterfaceDeclaration a = file.assertDeclaration();
      MethodDeclaration decl = (MethodDeclaration)a.getMembers().get(0);
      decl.getAnnotations().clear();
      helper.saveJavaFile(file);
      assertDelete(helper.getSourcePath().getPath("model", "meta", "controller", "package-info.java"));

      //
      helper.addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, ser);

      //
      JSON expected = new JSON()
         .list("applications", new JSON().
            list("controllers").
            set("fqn", "model.meta.controller.ControllerApplication").
            set("handle", "ElementHandle.Package[qn=model.meta.controller]").
            list("templates")
         );
      assertEquals(expected, mm.toJSON());

      //
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(1, events.size());
      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
      assertTrue(events.get(0).getObject() instanceof ControllerMetaModel);
   }

   @Test
   public void testAddMethod() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller");
      JavaFile file = helper.assertJavaFile("model", "meta", "controller", "A.java");
      ClassOrInterfaceDeclaration a = file.assertDeclaration();
      MethodDeclaration decl = (MethodDeclaration)a.getMembers().get(0);
      assertTrue(a.getMembers().remove(decl));
      helper.saveJavaFile(file);
      helper.assertCompile();

      //
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(1, events.size());
      assertEquals(MetaModelEvent.AFTER_ADD, events.get(0).getType());
      assertTrue(events.get(0).getObject() instanceof ApplicationMetaModel);
      Tools.serialize(mm, ser);

      //
      a.getMembers().add(decl);
      helper.saveJavaFile(file);
      assertDelete(helper.getSourcePath().getPath("model", "meta", "controller", "package-info.java"));
      assertDelete(helper.getClassOutput().getPath("model", "meta", "controller", "A.class"));
      helper.addClassPath(helper.getClassOutput()).assertCompile();

      //
      mm = Tools.unserialize(MetaModel.class, ser);

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
      events = mm.getQueue().clear();
      assertEquals(1, events.size());
      assertEquals(MetaModelEvent.AFTER_ADD, events.get(0).getType());
      assertTrue(events.get(0).getObject() instanceof ControllerMetaModel);
   }

   @Test
   public void testRemoveSingleMethod() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller");
      helper.assertCompile();
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      JavaFile file = helper.assertJavaFile("model", "meta", "controller", "A.java");
      ClassOrInterfaceDeclaration a = file.assertDeclaration();
      a.getMembers().clear();
      helper.saveJavaFile(file);
      assertDelete(helper.getSourcePath().getPath("model", "meta", "controller", "package-info.java"));

      //
      helper.addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, ser);

      //
      JSON expected = new JSON()
         .list("applications", new JSON().
            list("controllers").
            set("fqn", "model.meta.controller.ControllerApplication").
            set("handle", "ElementHandle.Package[qn=model.meta.controller]").
            list("templates")
         );
      assertEquals(expected, mm.toJSON());

      //
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(1, events.size());
      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
      assertTrue(events.get(0).getObject() instanceof ControllerMetaModel);
   }

   @Test
   public void testRemoveMethod() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller");

      //
      JavaFile file = helper.assertJavaFile("model", "meta", "controller", "A.java");
      ClassOrInterfaceDeclaration a = file.assertDeclaration();
      MethodDeclaration show = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "show");
      show.setAnnotations(Collections.<AnnotationExpr>singletonList(new NormalAnnotationExpr(ASTHelper.createNameExpr(View.class.getName()), Collections.<MemberValuePair>emptyList())));
      show.setBody(new BlockStmt());
      a.getMembers().add(show);
      helper.saveJavaFile(file);
      helper.assertCompile();

      //
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      assertTrue(a.getMembers().remove(show));
      helper.saveJavaFile(file);
      assertDelete(helper.getClassOutput().getPath("model", "meta", "controller", "A.class"));
      helper.addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, ser);

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
      assertEquals(1, events.size());
      assertEquals(MetaModelEvent.UPDATED, events.get(0).getType());
      assertTrue(events.get(0).getObject() instanceof ApplicationMetaModel);
   }

   @Test
   public void testRemoveOverloadedMethod() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller");
      JavaFile file = helper.assertJavaFile("model", "meta", "controller", "A.java");
      ClassOrInterfaceDeclaration a = file.assertDeclaration();
      MethodDeclaration index = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "index");
      ASTHelper.addParameter(index, ASTHelper.createParameter(new ClassOrInterfaceType(String.class.getName()), "s"));
      index.setAnnotations(Collections.<AnnotationExpr>singletonList(new NormalAnnotationExpr(ASTHelper.createNameExpr(View.class.getName()), Collections.<MemberValuePair>emptyList())));
      index.setBody(new BlockStmt());
      a.getMembers().add(index);
      helper.saveJavaFile(file);
      helper.assertCompile();

      //
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      assertTrue(a.getMembers().remove(index));
      helper.saveJavaFile(file);
      assertDelete(helper.getClassOutput().getPath("model", "meta", "controller", "A.class"));

      //
      helper.addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, ser);

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
      assertEquals(1, events.size());
      assertEquals(MetaModelEvent.UPDATED, events.get(0).getType());
      assertTrue(events.get(0).getObject() instanceof ApplicationMetaModel);
   }

   @Test
   public void testRefactorPackageName() throws Exception
   {
      CompilerHelper<File, File> helper = compiler("model", "meta", "controller");
      helper.assertCompile();

      //
      File ser = helper.getSourceOutput().getPath("org", "juzu", "metamodel.ser");
      MetaModel mm = Tools.unserialize(MetaModel.class, ser);
      mm.getQueue().clear();
      Tools.serialize(mm, ser);

      //
      File file = helper.getSourcePath().getPath("model", "meta", "controller", "A.java");
      File sub = new File(file.getParentFile(), "sub");
      assertTrue(sub.mkdir());
      File tmp = new File(sub, file.getName());
      assertTrue(file.renameTo(tmp));
      JavaFile javaFile = helper.assertJavaFile("model", "meta", "controller", "sub", "A.java");
      javaFile.getCompilationUnit().getPackage().setName(ASTHelper.createNameExpr("model.meta.controller.sub"));
      helper.saveJavaFile(javaFile);

      //
      assertDelete(helper.getClassOutput().getPath("model", "meta", "controller", "A.class"));
      helper.addClassPath(helper.getClassOutput()).assertCompile();
      mm = Tools.unserialize(MetaModel.class, ser);

      //
      List<MetaModelEvent> events = mm.getQueue().clear();
      assertEquals(3, events.size());
      assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
      assertEquals(ElementHandle.Class.create(new FQN("model.meta.controller.A")), ((ControllerMetaModel)events.get(0).getObject()).getHandle());
      assertEquals(MetaModelEvent.AFTER_ADD, events.get(1).getType());
      assertEquals(ElementHandle.Class.create(new FQN("model.meta.controller.sub.A")), ((ControllerMetaModel)events.get(1).getObject()).getHandle());
      assertEquals(MetaModelEvent.UPDATED, events.get(2).getType());
      assertEquals(ElementHandle.Package.create(new QN("model.meta.controller")), ((ApplicationMetaModel)events.get(2).getObject()).getHandle());
   }
}
