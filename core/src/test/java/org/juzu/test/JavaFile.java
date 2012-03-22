package org.juzu.test;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.TypeDeclaration;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JavaFile
{

   /** . */
   final String[] names;
   
   /** . */
   final CompilationUnit cu;

   public JavaFile(String[] names, CompilationUnit cu)
   {
      this.names = names;
      this.cu = cu;
   }
   
   public ClassOrInterfaceDeclaration assertDeclaration()
   {
      List<TypeDeclaration> decls = cu.getTypes();
      AbstractTestCase.assertEquals(1, decls.size());
      TypeDeclaration decl = decls.get(0);
      return AbstractTestCase.assertInstanceOf(ClassOrInterfaceDeclaration.class, decl);
   }
}
