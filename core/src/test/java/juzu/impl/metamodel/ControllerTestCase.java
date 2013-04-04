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

package juzu.impl.metamodel;

import japa.parser.ASTHelper;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import juzu.Action;
import juzu.View;
import juzu.impl.common.Name;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.plugin.controller.metamodel.ControllerMetaModel;
import juzu.impl.plugin.controller.metamodel.ControllersMetaModel;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.impl.plugin.controller.metamodel.MethodMetaModel;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import juzu.test.JavaFile;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static juzu.impl.common.JSON.json;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerTestCase extends AbstractTestCase {

  @Test
  public void testBuild() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.controller");
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    ModuleMetaModel mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

    //
    JSON expected = json()
      .set("applications", json().
          list("values", json().
              list("controllers", json().
                  set("handle", "ElementHandle.Class[fqn=metamodel.controller.A]").
                  list("methods", json().
                      set("handle", "ElementHandle.Method[fqn=metamodel.controller.A,name=index,parameterTypes[]]").
                      set("id", null).
                      set("name", "index").
                      list("parameters").
                      set("phase", "VIEW")
                  )
              ).
              set("handle", "ElementHandle.Package[qn=metamodel.controller]").
              list("templates")
          )
      );
    assertEquals(expected, mm.toJSON());

    //
    List<MetaModelEvent> events = mm.getQueue().clear();
    ApplicationMetaModel application = mm.getChildren(ApplicationMetaModel.class).iterator().next();
    ControllerMetaModel controller = application.getChild(ControllersMetaModel.KEY).iterator().next();
    MethodMetaModel method = controller.getMethods().iterator().next();
    assertEquals(Arrays.asList(
      MetaModelEvent.createAdded(application),
      MetaModelEvent.createAdded(controller),
      MetaModelEvent.createAdded(method),
      MetaModelEvent.createUpdated(controller)
    ), events);
  }

  @Test
  public void testRemoveApplication() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.controller");
    helper.assertCompile();
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState unserialize = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)unserialize.metaModel;
    mm.getQueue().clear();
    Tools.serialize(unserialize, ser);

    //
    assertDelete(helper.getSourcePath().getPath("metamodel", "controller", "package-info.java"));

    //
    helper.addClassPath(helper.getClassOutput()).assertCompile();
    mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

    //
    JSON expected = json().
      set("applications", json().
        list("values")
      );
    assertEquals(expected, mm.toJSON());

    //
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(3, events.size());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
    assertInstanceOf(MethodMetaModel.class, events.get(0).getObject());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(1).getType());
    assertInstanceOf(ControllerMetaModel.class, events.get(1).getObject());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(2).getType());
    assertInstanceOf(ApplicationMetaModel.class, events.get(2).getObject());
  }

  @Test
  public void testRemoveController() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.controller");
    helper.assertCompile();
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState unserialize = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)unserialize.metaModel;
    mm.getQueue().clear();
    Tools.serialize(unserialize, ser);

    //
    assertDelete(helper.getSourcePath().getPath("metamodel", "controller", "A.java"));

    //
    helper.addClassPath(helper.getClassOutput()).assertCompile();
    mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

    //
    JSON expected = json()
      .set("applications", json().
        list("values", json().
          list("controllers").
          set("handle", "ElementHandle.Package[qn=metamodel.controller]").
          list("templates")
        )
      );
    assertEquals(expected, mm.toJSON());

    //
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(2, events.size());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
    assertInstanceOf(MethodMetaModel.class, events.get(0).getObject());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(1).getType());
    assertInstanceOf(ControllerMetaModel.class, events.get(1).getObject());
  }

  @Test
  public void testChangeAnnotation() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.controller");
    helper.assertCompile();
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState unserialize = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)unserialize.metaModel;
    mm.getQueue().clear();
    Tools.serialize(unserialize, ser);

    //
    JavaFile file = helper.assertSource("metamodel", "controller", "A.java");
    ClassOrInterfaceDeclaration a = file.assertDeclaration();
    MethodDeclaration decl = (MethodDeclaration)a.getMembers().get(0);
    decl.getAnnotations().get(0).setName(ASTHelper.createNameExpr(Action.class.getName()));
    file.assertSave();

    //
    helper.addClassPath(helper.getClassOutput()).assertCompile();
    mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

    //
    JSON expected = json()
      .set("applications", json().
        list("values", json().
          list("controllers", json().
            set("handle", "ElementHandle.Class[fqn=metamodel.controller.A]").
            list("methods", json().
              set("handle", "ElementHandle.Method[fqn=metamodel.controller.A,name=index,parameterTypes[]]").
              set("id", null).
              set("name", "index").
              list("parameters").
              set("phase", "ACTION")
            )
          ).
          set("handle", "ElementHandle.Package[qn=metamodel.controller]").
          list("templates")
        )
      );
    assertEquals(expected, mm.toJSON());

    //
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(5, events.size());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
    assertInstanceOf(MethodMetaModel.class, events.get(0).getObject());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(1).getType());
    assertInstanceOf(ControllerMetaModel.class, events.get(1).getObject());
    assertEquals(MetaModelEvent.AFTER_ADD, events.get(2).getType());
    assertInstanceOf(ControllerMetaModel.class, events.get(2).getObject());
    assertEquals(MetaModelEvent.AFTER_ADD, events.get(3).getType());
    assertInstanceOf(MethodMetaModel.class, events.get(3).getObject());
    assertEquals(MetaModelEvent.UPDATED, events.get(4).getType());
    assertInstanceOf(ControllerMetaModel.class, events.get(4).getObject());
  }

  @Test
  public void testRemoveAnnotation() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.controller");
    helper.assertCompile();
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState unserialize = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)unserialize.metaModel;
    mm.getQueue().clear();
    Tools.serialize(unserialize, ser);

    //
    JavaFile file = helper.assertSource("metamodel", "controller", "A.java");
    ClassOrInterfaceDeclaration a = file.assertDeclaration();
    MethodDeclaration decl = (MethodDeclaration)a.getMembers().get(0);
    decl.getAnnotations().clear();
    file.assertSave();

    //
    helper.addClassPath(helper.getClassOutput()).assertCompile();
    mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

    //
    JSON expected = json()
      .set("applications", json().
        list("values", json().
          list("controllers").
          set("handle", "ElementHandle.Package[qn=metamodel.controller]").
          list("templates")
        )
      );
    assertEquals(expected, mm.toJSON());

    //
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(2, events.size());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
    assertInstanceOf(MethodMetaModel.class, events.get(0).getObject());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(1).getType());
    assertInstanceOf(ControllerMetaModel.class, events.get(1).getObject());
  }

  @Test
  public void testAddMethod() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.controller");
    JavaFile file = helper.assertSource("metamodel", "controller", "A.java");
    ClassOrInterfaceDeclaration a = file.assertDeclaration();
    MethodDeclaration decl = (MethodDeclaration)a.getMembers().get(0);
    assertTrue(a.getMembers().remove(decl));
    file.assertSave();
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState unserialize = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)unserialize.metaModel;
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(1, events.size());
    assertEquals(MetaModelEvent.AFTER_ADD, events.get(0).getType());
    assertTrue(events.get(0).getObject() instanceof ApplicationMetaModel);
    Tools.serialize(unserialize, ser);

    //
    a.getMembers().add(decl);
    file.assertSave();
    helper.addClassPath(helper.getClassOutput()).assertCompile();

    //
    mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

    //
    JSON expected = json()
      .set("applications", json().
        list("values", json().
          list("controllers", json().
            set("handle", "ElementHandle.Class[fqn=metamodel.controller.A]").
            list("methods", json().
              set("handle", "ElementHandle.Method[fqn=metamodel.controller.A,name=index,parameterTypes[]]").
              set("id", null).
              set("name", "index").
              list("parameters").
              set("phase", "VIEW")
            )
          ).
          set("handle", "ElementHandle.Package[qn=metamodel.controller]").
          list("templates")
        )
      );
    assertEquals(expected, mm.toJSON());

    //
    events = mm.getQueue().clear();
    assertEquals(3, events.size());
    assertEquals(MetaModelEvent.AFTER_ADD, events.get(0).getType());
    assertInstanceOf(ControllerMetaModel.class, events.get(0).getObject());
    assertEquals(MetaModelEvent.AFTER_ADD, events.get(1).getType());
    assertInstanceOf(MethodMetaModel.class, events.get(1).getObject());
    assertEquals(MetaModelEvent.UPDATED, events.get(2).getType());
    assertInstanceOf(ControllerMetaModel.class, events.get(2).getObject());
  }

  @Test
  public void testRemoveSingleMethod() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.controller");
    helper.assertCompile();
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState unserialize = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)unserialize.metaModel;
    mm.getQueue().clear();
    Tools.serialize(unserialize, ser);

    //
    JavaFile file = helper.assertSource("metamodel", "controller", "A.java");
    ClassOrInterfaceDeclaration a = file.assertDeclaration();
    a.getMembers().clear();
    file.assertSave();

    //
    helper.addClassPath(helper.getClassOutput()).assertCompile();
    mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

    //
    JSON expected = json()
      .set("applications", json().
        list("values", json().
          list("controllers").
          set("handle", "ElementHandle.Package[qn=metamodel.controller]").
          list("templates")
        )
      );
    assertEquals(expected, mm.toJSON());

    //
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(2, events.size());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
    assertInstanceOf(MethodMetaModel.class, events.get(0).getObject());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(1).getType());
    assertInstanceOf(ControllerMetaModel.class, events.get(1).getObject());
  }

  @Test
  public void testRemoveMethod() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.controller");

    //
    JavaFile file = helper.assertSource("metamodel", "controller", "A.java");
    ClassOrInterfaceDeclaration a = file.assertDeclaration();
    MethodDeclaration show = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "show");
    show.setAnnotations(Collections.<AnnotationExpr>singletonList(new NormalAnnotationExpr(ASTHelper.createNameExpr(View.class.getName()), Collections.<MemberValuePair>emptyList())));
    show.setBody(new BlockStmt());
    a.getMembers().add(show);
    file.assertSave();
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState unserialize = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)unserialize.metaModel;
    mm.getQueue().clear();
    Tools.serialize(unserialize, ser);

    //
    assertTrue(a.getMembers().remove(show));
    file.assertSave();
    helper.addClassPath(helper.getClassOutput()).assertCompile();
    mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

    //
    JSON expected = json()
      .set("applications", json().
        list("values", json().
          list("controllers", json().
            set("handle", "ElementHandle.Class[fqn=metamodel.controller.A]").
            list("methods", json().
              set("handle", "ElementHandle.Method[fqn=metamodel.controller.A,name=index,parameterTypes[]]").
              set("id", null).
              set("name", "index").
              list("parameters").
              set("phase", "VIEW")
            )
          ).
          set("handle", "ElementHandle.Package[qn=metamodel.controller]").
          list("templates")
        )
      );
    assertEquals(expected, mm.toJSON());

    //
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(1, events.size());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
    assertInstanceOf(MethodMetaModel.class, events.get(0).getObject());
  }

  @Test
  public void testRemoveOverloadedMethod() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.controller");
    JavaFile file = helper.assertSource("metamodel", "controller", "A.java");
    ClassOrInterfaceDeclaration a = file.assertDeclaration();
    MethodDeclaration index = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "index");
    ASTHelper.addParameter(index, ASTHelper.createParameter(new ClassOrInterfaceType(String.class.getName()), "s"));
    index.setAnnotations(Collections.<AnnotationExpr>singletonList(new NormalAnnotationExpr(ASTHelper.createNameExpr(View.class.getName()), Collections.<MemberValuePair>emptyList())));
    index.setBody(new BlockStmt());
    a.getMembers().add(index);
    file.assertSave();
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState unserialize = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)unserialize.metaModel;
    mm.getQueue().clear();
    Tools.serialize(unserialize, ser);

    //
    assertTrue(a.getMembers().remove(index));
    file.assertSave();

    //
    helper.addClassPath(helper.getClassOutput()).assertCompile();
    mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

    //
    JSON expected = json()
      .set("applications", json().
        list("values", json().
          list("controllers", json().
            set("handle", "ElementHandle.Class[fqn=metamodel.controller.A]").
            list("methods", json().
              set("handle", "ElementHandle.Method[fqn=metamodel.controller.A,name=index,parameterTypes[]]").
              set("id", null).
              set("name", "index").
              list("parameters").
              set("phase", "VIEW")
            )
          ).
          set("handle", "ElementHandle.Package[qn=metamodel.controller]").
          list("templates")
        )
      );
    assertEquals(expected, mm.toJSON());

    //
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(1, events.size());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
    assertInstanceOf(MethodMetaModel.class, events.get(0).getObject());
  }

  @Test
  public void testRefactorPackageName() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.controller");
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    MetaModelState unserialize = Tools.unserialize(MetaModelState.class, ser);
    ModuleMetaModel mm = (ModuleMetaModel)unserialize.metaModel;
    mm.getQueue().clear();
    Tools.serialize(unserialize, ser);

    //
    File file = helper.getSourcePath().getPath("metamodel", "controller", "A.java");
    File sub = new File(file.getParentFile(), "sub");
    assertTrue(sub.mkdir());
    File tmp = new File(sub, file.getName());
    assertTrue(file.renameTo(tmp));
    JavaFile javaFile = helper.assertSource("metamodel", "controller", "sub", "A.java");
    javaFile.assertCompilationUnit().getPackage().setName(ASTHelper.createNameExpr("metamodel.controller.sub"));
    javaFile.assertSave();

    //
    helper.addClassPath(helper.getClassOutput()).assertCompile();
    mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;

    //
    List<MetaModelEvent> events = mm.getQueue().clear();
    assertEquals(5, events.size());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(0).getType());
    assertEquals(ElementHandle.Method.create(Name.parse("metamodel.controller.A"), "index", Collections.<String>emptyList()), ((MethodMetaModel)events.get(0).getObject()).getHandle());
    assertEquals(MetaModelEvent.BEFORE_REMOVE, events.get(1).getType());
    assertEquals(ElementHandle.Class.create(Name.parse("metamodel.controller.A")), ((ControllerMetaModel)events.get(1).getObject()).getHandle());
    assertEquals(MetaModelEvent.AFTER_ADD, events.get(2).getType());
    assertEquals(ElementHandle.Class.create(Name.parse("metamodel.controller.sub.A")), ((ControllerMetaModel)events.get(2).getObject()).getHandle());
    assertEquals(MetaModelEvent.AFTER_ADD, events.get(3).getType());
    assertEquals(ElementHandle.Method.create(Name.parse("metamodel.controller.sub.A"), "index", Collections.<String>emptyList()), ((MethodMetaModel)events.get(3).getObject()).getHandle());
    assertEquals(MetaModelEvent.UPDATED, events.get(4).getType());
    assertEquals(ElementHandle.Class.create(Name.parse("metamodel.controller.sub.A")), ((ControllerMetaModel)events.get(4).getObject()).getHandle());
  }
}
