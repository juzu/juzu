package org.juzu.impl.compiler;

import junit.framework.TestCase;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.impl.spi.fs.ram.RAMDir;
import org.juzu.impl.spi.fs.ram.RAMFile;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilationTestCase extends TestCase
{

   public void testBar() throws Exception
   {
      File root = new File(System.getProperty("test.resources"));
      CompilerContext<File, File, File> ctx = new CompilerContext<File, File, File>(new DiskFileSystem(root));
      Map<String, ClassFile> files = ctx.compile();
      assertEquals(1, files.size());
   }

   public void testChange() throws Exception
   {
      RAMFileSystem ramFS = new RAMFileSystem();
      RAMDir root = ramFS.getRoot();
      RAMDir foo = root.addDir("foo");
      RAMFile a = foo.addFile("A.java").update("package foo; public class A {}");
      RAMFile b = foo.addFile("B.java").update("package foo; public class B {}");

      CompilerContext<RAMPath, RAMDir, RAMFile> compiler = new CompilerContext<RAMPath, RAMDir, RAMFile>(ramFS);
      Map<String, ClassFile> res = compiler.compile();
      assertEquals(2, res.size());
      ClassFile aClass = res.get("foo.A");
      assertNotNull(aClass);
      ClassFile bClass = res.get("foo.B");
      assertNotNull(bClass);

      //
      while (true)
      {
         b.update("package foo; public class B extends A {}");
         if (bClass.getLastModified() < b.getLastModified())
         {
            break;
         }
         else
         {
            Thread.sleep(1);
         }
      }


      res = compiler.compile();
      assertEquals(1, res.size());
      bClass = res.get("foo.B");
      assertNotNull(bClass);
   }

   @javax.annotation.processing.SupportedAnnotationTypes({"*"})
   @javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
   public static class ProcessorImpl extends AbstractProcessor
   {

      /** . */
      final List<String> names = new ArrayList<String>();

      /** . */
      private boolean done;

      @Override
      public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
      {
         for (Element elt : roundEnv.getRootElements())
         {
            if (elt instanceof TypeElement)
            {
               TypeElement typeElt = (TypeElement)elt;
               names.add(typeElt.getQualifiedName().toString());
            }
         }

         //
         if (!done)
         {
            try
            {
               Filer filer = processingEnv.getFiler();
               JavaFileObject b = filer.createSourceFile("B");
               PrintWriter writer = new PrintWriter(b.openWriter());
               writer.println("public class B { }");
               writer.close();
               done = true;
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }

         //
         return true;
      }
   }

   public void testProcessor() throws Exception
   {
      RAMFileSystem ramFS = new RAMFileSystem();
      RAMDir root = ramFS.getRoot();
      RAMFile a = root.addFile("A.java").update("public class A {}");

      CompilerContext<RAMPath, RAMDir, RAMFile> compiler = new CompilerContext<RAMPath, RAMDir, RAMFile>(ramFS);
      ProcessorImpl processor = new ProcessorImpl();
      compiler.addAnnotationProcessor(processor);
      Map<String, ClassFile> res = compiler.compile();
      assertNotNull(res);
      assertEquals(2, res.size());
      assertEquals(Arrays.asList("A", "B"), processor.names);
   }
}
