/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package juzu.impl.metamodel;

import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.compiler.CompilationError;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import juzu.test.JavaFile;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static juzu.impl.common.JSON.json;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ParamTestCase extends AbstractTestCase {

  /** . */
  private static JSON expectedJSON = json()
    .set("applications", json().
      list("values", json().
        list("controllers", json().
          set("handle", "ElementHandle.Class[fqn=metamodel.param.A]").
          list("methods", json().
            set("handle", "ElementHandle.Method[fqn=metamodel.param.A,name=index,parameterTypes[metamodel.param.Bean]]").
            set("id", null).
            set("name", "index").
            set("phase", "VIEW").
            list("parameters", json().
              set("name", "bean").
              set("declaredType", "metamodel.param.Bean").
              set("type", "ElementHandle.Class[fqn=metamodel.param.Bean]").
              set("cardinality", "SINGLE")
            )
          )
        ).
        set("handle", "ElementHandle.Package[qn=metamodel.param]").
        list("templates")
      )
    );

  @Test
  public void testBuild() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel", "param");
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    ModuleMetaModel mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;;
    assertEquals(expectedJSON, mm.toJSON());
  }

  @Test
  public void testParam() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel", "param");
    helper.assertCompile();

    // Remove @Param
    JavaFile file = helper.assertJavaFile("metamodel", "param", "Bean.java");
    ClassOrInterfaceDeclaration bean = file.assertDeclaration();
    AnnotationExpr annotation = bean.getAnnotations().get(0);
    bean.getAnnotations().clear();
    file.assertSave();
//      helper.assertRemove("metamodel", "param", "A.java");

    // Recompile
    // we should have a way to test the error kind more precisely
    List<CompilationError> errors = helper.addClassPath(helper.getClassOutput()).failCompile();
    assertEquals(1, errors.size());

    // Add back @Param
    bean.getAnnotations().add(annotation);
    file.assertSave();

    // Recompile
    helper.addClassPath(helper.getClassOutput()).assertCompile();

    // Check
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    ModuleMetaModel mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;;
    assertEquals(expectedJSON, mm.toJSON());
  }
}
