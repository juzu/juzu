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
public class JavaFile<I> {

  /** . */
  final ReadWriteFileSystem<I> sourcePath;

  /** . */
  final I path;

  /** . */
  private CompilationUnit cu;

  public JavaFile(ReadWriteFileSystem<I> sourcePath, I path) {
    this.sourcePath = sourcePath;
    this.path = path;
    this.cu = null;
  }

  public CompilationUnit assertCompilationUnit() {
    if (cu == null) {
      try {
        Content content = sourcePath.getContent(path);
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

  public void assertTouch() {
    try {
      Content content = sourcePath.getContent(path);
      sourcePath.setContent(path, content);
    }
    catch (Exception e) {
      throw AbstractTestCase.failure(e);
    }
  }

  public void assertSave() {
    try {
      String s = cu.toString();
      sourcePath.setContent(path, new Content(0, s));
    }
    catch (Exception e) {
      throw AbstractTestCase.failure(e);
    }
  }
}
