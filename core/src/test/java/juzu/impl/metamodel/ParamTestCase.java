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
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.param");
    helper.assertCompile();

    //
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    ModuleMetaModel mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;
    assertEquals(expectedJSON, mm.toJSON());
  }

  @Test
  public void testParam() throws Exception {
    CompilerAssert<File, File> helper = incrementalCompiler("metamodel.param");
    helper.assertCompile();

    // Remove @Param
    JavaFile file = helper.assertSource("metamodel", "param", "Bean.java");
    ClassOrInterfaceDeclaration bean = file.assertDeclaration();
    AnnotationExpr annotation = bean.getAnnotations().get(0);
    bean.getAnnotations().clear();
    file.assertSave();
//      helper.assertRemove("metamodel", "param", "A.java");

    // Recompile
    // we should have a way to test the error kind more precisely
    /*List<CompilationError> errors = */helper.assertCompile();
    // assertEquals(1, errors.size());

    // Add back @Param
    bean.getAnnotations().add(annotation);
    file.assertSave();

    // Recompile
    helper.assertCompile();

    // Check
    File ser = helper.getSourceOutput().getPath("juzu", "metamodel.ser");
    ModuleMetaModel mm = (ModuleMetaModel)Tools.unserialize(MetaModelState.class, ser).metaModel;
    assertEquals(expectedJSON, mm.toJSON());
  }
}
