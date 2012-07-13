package juzu.test;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
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
