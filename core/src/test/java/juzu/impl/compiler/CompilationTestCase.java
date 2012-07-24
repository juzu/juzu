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

package juzu.impl.compiler;

import junit.framework.AssertionFailedError;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.ram.RAMDir;
import juzu.impl.fs.spi.ram.RAMFile;
import juzu.impl.fs.spi.ram.RAMFileSystem;
import juzu.impl.fs.spi.ram.RAMPath;
import juzu.impl.common.Content;
import juzu.impl.common.Tools;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import org.junit.Test;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilationTestCase extends AbstractTestCase {

  @Test
  public void testErrorCodePattern() {
    asserNotMatch("");
    asserNotMatch("[]");
    asserNotMatch("[a]");
    asserNotMatch("[]()");
    asserNotMatch("[](a)");
    asserMatch("[a]()", "a", "");
    asserMatch("[a](b)", "a", "b");
    asserMatch("[ERROR_01](5,foobar)", "ERROR_01", "5,foobar");
  }

  private void asserNotMatch(String test) {
    Matcher matcher = Compiler.PATTERN.matcher(test);
    assertFalse("Was not expecting " + Compiler.PATTERN + " to match " + test, matcher.matches());
  }

  private void asserMatch(String test, String expectedCode, String expectedArguments) {
    Matcher matcher = Compiler.PATTERN.matcher(test);
    assertTrue("Was expecting " + Compiler.PATTERN + " to match " + test, matcher.matches());
    assertEquals(expectedCode, matcher.group(1));
    assertEquals(expectedArguments, matcher.group(2));
  }

  @Test
  public void testBar() throws Exception {
    CompilerAssert<File, File> helper = compiler("compiler", "disk");
    helper.with(null);
    helper.assertCompile();
    assertEquals(1, helper.getClassOutput().size(ReadFileSystem.FILE));
  }

  @Test
  public void testGetResourceFromProcessor() throws Exception {
    DiskFileSystem input = diskFS("compiler", "getresource");

    //
    @javax.annotation.processing.SupportedAnnotationTypes({"*"})
    @javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
    class ProcessorImpl extends AbstractProcessor {

      /** . */
      Object result = null;

      @Override
      public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
          try {
            Filer filer = processingEnv.getFiler();
            FileObject o = filer.getResource(StandardLocation.SOURCE_PATH, "compiler.getresource", "A.txt");
            result = o.getCharContent(false);
          }
          catch (IOException e) {
            result = e;
          }
        }
        return false;
      }
    }

    //
    RAMFileSystem output = new RAMFileSystem();
    Compiler compiler = Compiler.builder().sourcePath(input).output(output).build();
    ProcessorImpl processor = new ProcessorImpl();
    compiler.addAnnotationProcessor(processor);
    compiler.compile();
    assertEquals(1, output.size(ReadFileSystem.FILE));
    if (processor.result instanceof Exception) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause((Throwable)processor.result);
      throw afe;
    }
    else if (processor.result instanceof CharSequence) {
      assertEquals("value", processor.result.toString());
    }
    else {
      fail("Was not expecting result to be " + processor.result);
    }
  }

  // For now we don't support this until we figure the feature fully
  public void _testChange() throws Exception {
    RAMFileSystem ramFS = new RAMFileSystem();
    RAMPath root = ramFS.getRoot();
    RAMPath foo = root.addDir("foo");
    RAMPath a = foo.addFile("A.java").update("package foo; public class A {}");
    RAMPath b = foo.addFile("B.java").update("package foo; public class B {}");

    RAMFileSystem output = new RAMFileSystem();
    Compiler compiler = Compiler.builder().sourcePath(ramFS).output(output).build();
    compiler.compile();
    assertEquals(2, output.size(ReadFileSystem.FILE));
    Content aClass = output.getContent("foo", "A");
    assertNotNull(aClass);
    Content bClass = output.getContent("foo", "B");
    assertNotNull(bClass);

    //
    while (true) {
      b.update("package foo; public class B extends A {}");
      if (bClass.getLastModified() < b.getLastModified()) {
        break;
      }
      else {
        Thread.sleep(1);
      }
    }

    //
    compiler.compile();
    assertEquals(1, output.size(ReadFileSystem.FILE));
    bClass = output.getContent("foo", "B");
    assertNotNull(bClass);
  }

  @javax.annotation.processing.SupportedAnnotationTypes({"*"})
  @javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
  public static class ProcessorImpl extends AbstractProcessor {

    /** . */
    final List<String> names = new ArrayList<String>();

    /** . */
    private boolean done;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
      for (Element elt : roundEnv.getRootElements()) {
        if (elt instanceof TypeElement) {
          TypeElement typeElt = (TypeElement)elt;
          names.add(typeElt.getQualifiedName().toString());
        }
      }

      //
      if (!done) {
        try {
          Filer filer = processingEnv.getFiler();
          JavaFileObject b = filer.createSourceFile("compiler.processor.B");
          PrintWriter writer = new PrintWriter(b.openWriter());
          writer.println("package compiler.processor; public class B { }");
          writer.close();
          done = true;
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }

      //
      return false;
    }
  }

  @Test
  public void testProcessor() throws Exception {
    DiskFileSystem ramFS = diskFS("compiler", "processor");
    RAMFileSystem sourceOutput = new RAMFileSystem();
    RAMFileSystem classOutput = new RAMFileSystem();
    Compiler compiler = Compiler.builder().sourcePath(ramFS).sourceOutput(sourceOutput).classOutput(classOutput).build();
    ProcessorImpl processor = new ProcessorImpl();
    compiler.addAnnotationProcessor(processor);
    compiler.compile();
    assertEquals(2, classOutput.size(ReadFileSystem.FILE));
    assertEquals(Arrays.asList("compiler.processor.A", "compiler.processor.B"), processor.names);
    assertEquals(1, sourceOutput.size(ReadFileSystem.FILE));
  }

  @Test
  public void testCompilationFailure() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("compiler", "failure");
    assertEquals(1, compiler.failCompile().size());
  }

  @Test
  public void testAnnotationException() throws Exception {
    DiskFileSystem fs = diskFS("compiler", "annotationexception");

    //
    Compiler compiler = Compiler.builder().sourcePath(fs).output(new RAMFileSystem()).build();
    @javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
    @javax.annotation.processing.SupportedAnnotationTypes({"*"})
    class Processor1 extends AbstractProcessor {
      @Override
      public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Deprecated.class);
        if (elements.size() == 1) {
          Element elt = elements.iterator().next();
          processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "the_message", elt);
        }
        return false;
      }
    }
    compiler.addAnnotationProcessor(new Processor1());
    try {
      compiler.compile();
      fail();
    }
    catch (CompilationException e) {
      List<CompilationError> errors = e.getErrors();
      assertEquals(1, errors.size());
      CompilationError error = errors.get(0);
      assertEquals(null, error.getCode());
      assertEquals(Collections.<String>emptyList(), error.getArguments());
      assertEquals("/compiler/annotationexception/A.java", error.getSource());
      assertTrue(error.getMessage().contains("the_message"));
      assertNotNull(error.getSourceFile());
      assertTrue(error.getSourceFile().getAbsolutePath().endsWith("compiler/annotationexception/A.java"));
      assertNotNull(error.getLocation());
    }

    //
    compiler = Compiler.builder().sourcePath(fs).output(new RAMFileSystem()).build();
    @javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
    @javax.annotation.processing.SupportedAnnotationTypes({"*"})
    class Processor2 extends AbstractProcessor {
      boolean failed = false;

      @Override
      public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!failed) {
          failed = true;
          processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "the_message");
        }
        return false;
      }
    }
    compiler.addAnnotationProcessor(new Processor2());
    try {
      compiler.compile();
    }
    catch (CompilationException e) {
      List<CompilationError> errors = e.getErrors();
      assertEquals(1, errors.size());
      CompilationError error = errors.get(0);
      assertEquals(null, error.getCode());
      assertEquals(Collections.<String>emptyList(), error.getArguments());
      assertEquals(null, error.getSource());
      assertTrue(error.getMessage().contains("the_message"));
      assertNull(error.getSourceFile());
      assertNull(error.getLocation());
    }
  }

  @Test
  public void testErrorCode() throws IOException {
    final MessageCode code = new MessageCode("ERROR_01", "The error");
    class P extends BaseProcessor {
      @Override
      protected void doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessingException {
        if (roundEnv.processingOver()) {
          throw new ProcessingException(code, 5, "foobar");
        }
      }
    }

    DiskFileSystem fs = diskFS("compiler", "errorcode");
    Compiler compiler = Compiler.
      builder().
      config(new CompilerConfig().withProcessorOption("juzu.error_reporting", "formal")).
      sourcePath(fs).
      output(new RAMFileSystem()).
      build();
    P processor = new P();
    compiler.addAnnotationProcessor(processor);
    try {
      compiler.compile();
    }
    catch (CompilationException e) {
      List<CompilationError> errors = e.getErrors();
      assertEquals(1, errors.size());
      CompilationError error = errors.get(0);
      assertEquals(code, error.getCode());
      assertEquals(Arrays.asList("5", "foobar"), error.getArguments());
    }
  }

  @Test
  public void testIncremental() throws IOException, CompilationException {
    RAMFileSystem sourcePath = new RAMFileSystem();
    RAMFileSystem output = new RAMFileSystem();
    Compiler compiler = Compiler.builder().sourcePath(sourcePath).output(output).build();

    //
    RAMDir incremental = sourcePath.addDir(sourcePath.getRoot(), "compiler").addDir("incremental");
    RAMFile a = incremental.addFile("A.java").update("package compiler.incremental; public class A {}");
    compiler.compile("compiler/incremental/A.java");
    assertEquals(1, output.size(ReadFileSystem.FILE));
    a.del();

    //
    RAMFile b = incremental.addFile("B.java").update("package compiler.incremental; public class B extends A {}");
    compiler = Compiler.builder().sourcePath(sourcePath).addClassPath(output).output(output).build();
    compiler.compile("compiler/incremental/B.java");
  }

  @javax.annotation.processing.SupportedAnnotationTypes({"*"})
  @javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
  public static class ReadResource extends AbstractProcessor {

    /** . */
    private final StandardLocation location;

    public ReadResource(StandardLocation location) {
      this.location = location;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
      try {
        return _process(annotations, roundEnv);
      }
      catch (IOException e) {
        throw failure(e);
      }
    }

    private boolean _process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {
      if (roundEnv.processingOver()) {
        Filer filer = processingEnv.getFiler();

        // Read an existing resource
        FileObject foo = filer.getResource(location, "", "foo.txt");
        assertNotNull(foo);
        String s = Tools.read(foo.openInputStream());
        assertEquals("foo_value", s);

        // Now we overwrite the resource
        foo = filer.createResource(location, "", "foo.txt");
        OutputStream out = foo.openOutputStream();
        out.write("new_foo_value".getBytes());
        out.close();

        // Read an non existing resource
        // JDK 6 strange behavior / bug happens here, we should get bar=null but we don't
        // JDK 7 should return null
        FileObject bar = filer.getResource(location, "", "bar.txt");
        assertNotNull(bar);
        try {
          bar.openInputStream();
        }
        catch (IOException ignore) {
        }

        // Now create new resource
        foo = filer.createResource(location, "", "juu.txt");
        out = foo.openOutputStream();
        out.write("juu_value".getBytes());
        out.close();
      }
      return true;
    }
  }

  @Test
  public void testSourceOutputResource() throws IOException, CompilationException {
    testResource(StandardLocation.SOURCE_OUTPUT);
  }

  @Test
  public void testClassOutputResource() throws IOException, CompilationException {
    testResource(StandardLocation.CLASS_OUTPUT);
  }

  private void testResource(StandardLocation location) throws IOException, CompilationException {
    DiskFileSystem fs = diskFS("compiler", "missingresource");
    RAMFileSystem sourceOutput = new RAMFileSystem();
    RAMFileSystem classOutput = new RAMFileSystem();
    RAMFileSystem output;
    switch (location) {
      case SOURCE_OUTPUT:
        output = sourceOutput;
        break;
      case CLASS_OUTPUT:
        output = classOutput;
        break;
      default:
        throw failure("was not expecting " + location);
    }

    //
    output.addFile(output.getRoot(), "foo.txt").update("foo_value");
    Compiler compiler = Compiler.builder().sourcePath(fs).sourceOutput(sourceOutput).classOutput(classOutput).build();
    compiler.addAnnotationProcessor(new ReadResource(location));

    //
    compiler.compile();

    //
    RAMPath root = output.getRoot();
    Map<String, RAMFile> children = new HashMap<String, RAMFile>();
    for (RAMPath path : root.getChildren()) {
      if (path instanceof RAMFile) {
        children.put(path.getName(), (RAMFile)path);
      }
    }
    assertEquals(2, children.size());
    RAMFile foo = children.get("foo.txt");
    assertEquals("new_foo_value", foo.getContent().getCharSequence(Charset.defaultCharset()));
    RAMFile juu = children.get("juu.txt");
    assertEquals("juu_value", juu.getContent().getCharSequence(Charset.defaultCharset()).toString());
  }
}
