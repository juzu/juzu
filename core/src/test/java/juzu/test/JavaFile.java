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

package juzu.test;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import juzu.impl.fs.spi.ReadWriteFileSystem;
import juzu.impl.common.Content;

import java.io.InputStream;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JavaFile<I> extends FileResource<I> {

  /** . */
  private CompilationUnit cu;

  public JavaFile(ReadWriteFileSystem<I> sourcePath, I path) {
    super(sourcePath, path);

    //
    this.cu = null;
  }

  public CompilationUnit assertCompilationUnit() {
    if (cu == null) {
      try {
        Content content = sourcePath.getContent(path).getObject();
        InputStream in = content.getInputStream();
        cu = JavaParser.parse(in);
      }
      catch (Exception e) {
        throw AbstractTestCase.failure(e);
      }
    }
    return cu;
  }

  public ClassOrInterfaceDeclaration assertDeclaration() {
    List<TypeDeclaration> decls = assertCompilationUnit().getTypes();
    AbstractTestCase.assertEquals(1, decls.size());
    TypeDeclaration decl = decls.get(0);
    return AbstractTestCase.assertInstanceOf(ClassOrInterfaceDeclaration.class, decl);
  }

  public PackageDeclaration assertPackage() {
    return assertCompilationUnit().getPackage();
  }

  public void assertSave() {
    assertSave(cu.toString());
  }

}
