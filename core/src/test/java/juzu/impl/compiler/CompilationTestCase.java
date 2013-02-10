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
import juzu.impl.common.FileKey;
import juzu.impl.common.Name;
import juzu.impl.common.Timestamped;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.ReadWriteFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.ram.RAMFileSystem;
import juzu.impl.common.Content;
import juzu.impl.common.Tools;
import juzu.impl.metamodel.AnnotationState;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import juzu.test.JavaCompilerProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.inject.Provider;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(value = Parameterized.class)
public class CompilationTestCase extends AbstractTestCase {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{{JavaCompilerProvider.JAVAC},{JavaCompilerProvider.ECJ}});
  }

  /** . */
  private final JavaCompilerProvider compilerProvider;

  public CompilationTestCase(JavaCompilerProvider compilerProvider) {
    this.compilerProvider = compilerProvider;
  }

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
    CompilerAssert<File, File> helper = compiler("compiler.disk").with(compilerProvider);
    helper.with((Provider<? extends Processor>)null);
    helper.assertCompile();
    assertEquals(1, helper.getClassOutput().size(ReadFileSystem.FILE));
  }

  @Test
  public void testGetResourceFromProcessor() throws Exception {
    DiskFileSystem input = diskFS("compiler.getresource");

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
    Compiler compiler = Compiler.builder().javaCompiler(compilerProvider).sourcePath(input).output(output).build();
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
    String[] root = ramFS.getRoot();
    String[] foo = ramFS.makePath(root, "foo");
    String[] a = ramFS.makePath(foo, "A.java");
    ramFS.setContent(a, new Content("package foo; public class A {}"));
    String[] b = ramFS.makePath(foo, "B.java");
    ramFS.setContent(b, new Content("package foo; public class B {}"));

    //
    RAMFileSystem output = new RAMFileSystem();
    Compiler compiler = Compiler.builder().sourcePath(ramFS).output(output).build();
    compiler.compile();
    assertEquals(2, output.size(ReadFileSystem.FILE));
    Timestamped<Content> aClass = output.getContent(new String[]{"foo", "A"});
    assertNotNull(aClass);
    Timestamped<Content> bClass = output.getContent(new String[]{"foo", "B"});
    assertNotNull(bClass);

    //
    while (true) {
      ramFS.setContent(b, new Content("package foo; public class B extends A {}"));
      if (bClass.getTime() < ramFS.getLastModified(b)) {
        break;
      }
      else {
        Thread.sleep(1);
      }
    }

    //
    compiler.compile();
    assertEquals(1, output.size(ReadFileSystem.FILE));
    bClass = output.getContent(new String[]{"foo", "B"});
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
    ProcessorImpl processor = new ProcessorImpl();
    CompilerAssert<File, File> compiler = compiler("compiler.processor").with(compilerProvider).with(processor);
    compiler.assertCompile();
    assertEquals(2, compiler.getClassOutput().size(ReadFileSystem.FILE));
    assertEquals(Arrays.asList("compiler.processor.A", "compiler.processor.B"), processor.names);
    assertEquals(1, compiler.getSourceOutput().size(ReadFileSystem.FILE));
  }

  @Test
  public void testCompilationFailure() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("compiler.failure");
    assertEquals(1, compiler.failCompile().size());
  }

  @Test
  public void testProcessorErrorOnElement() throws Exception {
    DiskFileSystem fs = diskFS("compiler.annotationexception");
    Compiler compiler = Compiler.builder().javaCompiler(compilerProvider).sourcePath(fs).output(new RAMFileSystem()).build();
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
      assertEquals(fs.getPath("compiler", "annotationexception", "A.java"), error.getSourceFile());
      assertTrue(error.getMessage().contains("the_message"));
      assertNotNull(error.getSourceFile());
      assertNotNull(error.getLocation());
      String absolutePath = error.getSourceFile().getAbsolutePath();
      char separator = File.separatorChar;
      String[] absoluteNames = Tools.split(absolutePath, separator);
      assertTrue("Was expecting " + absolutePath + " to have at least three names ", absoluteNames.length > 3);
      assertEquals(
          "Was expecting " + absolutePath + " to end with compiler/annotationexceptions/A.java",
          Arrays.asList("compiler", "annotationexception", "A.java"),
          Arrays.asList(absoluteNames).subList(absoluteNames.length - 3, absoluteNames.length));
    }
  }

  @Test
  public void testProcessorError() throws Exception {
    // Works only with javac
    if (compilerProvider == JavaCompilerProvider.JAVAC) {
      DiskFileSystem fs = diskFS("compiler.annotationexception");
      Compiler compiler = Compiler.builder().javaCompiler(compilerProvider).sourcePath(fs).output(new RAMFileSystem()).build();
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

    DiskFileSystem fs = diskFS("compiler.errorcode");
    Compiler compiler = Compiler.
      builder().
      javaCompiler(compilerProvider).
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
    CompilerAssert<File, File> compiler = compiler(true, Name.parse("compiler.incremental"), "").
        with(compilerProvider).
        with((Provider<? extends Processor>)null);
    compiler.assertCompile();

    //
    ReadWriteFileSystem<File> classOutput = compiler.getClassOutput();
    assertEquals(1, classOutput.size(ReadFileSystem.FILE));

    //
    ReadWriteFileSystem<File> sourcePath = (ReadWriteFileSystem<File>)compiler.getSourcePath();
    File b = sourcePath.makePath(sourcePath.getPath("compiler", "incremental"), "B.java");
    sourcePath.setContent(b, new Content("package compiler.incremental; public class B extends A {}"));
    compiler.addClassPath(classOutput);
    compiler.assertCompile();
    assertEquals(2, classOutput.size(ReadFileSystem.FILE));
  }

  @javax.annotation.processing.SupportedAnnotationTypes({"*"})
  @javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
  public static class ReadResource extends AbstractProcessor {

    /** . */
    private final StandardLocation location;

    /** . */
    private ProcessingContext processingContext;

    public ReadResource(StandardLocation location) {
      this.location = location;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
      super.init(processingEnv);
      this.processingContext = new ProcessingContext(processingEnv);
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

        // Read an existing resource
        FileObject foo = processingContext.getResource(location, "", "foo.txt");
        assertNotNull(foo);
        String s = Tools.read(foo.openInputStream());
        assertEquals("foo_value", s);

        // Now we overwrite the resource
        foo = processingContext.createResource(location, "", "foo.txt");
        OutputStream out = foo.openOutputStream();
        out.write("new_foo_value".getBytes());
        out.close();

        // Read an non existing resource
        // JDK 6 strange behavior / bug happens here, we should get bar=null but we don't
        // JDK 7 should return null
        FileObject bar = processingContext.getResource(location, "", "bar.txt");
        assertNull(bar);

        // Now create new resource
        foo = processingContext.createResource(location, "", "juu.txt");
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
    CompilerAssert<File, File> compiler = compiler("compiler.missingresource").with(compilerProvider).with(new ReadResource(location));
    ReadWriteFileSystem<File> output;
    switch (location) {
      case SOURCE_OUTPUT:
        output = compiler.getSourceOutput();
        break;
      case CLASS_OUTPUT:
        output = compiler.getClassOutput();
        break;
      default:
        throw failure("was not expecting " + location);
    }

    //
    File foo = output.makePath(output.getRoot(), "foo.txt");
    output.setContent(foo, new Content("foo_value"));

    //
    compiler.assertCompile();

    //
    File root = output.getRoot();
    Map<String, File> children = new HashMap<String, File>();
    for (Iterator<File> i = output.getChildren(root);i.hasNext();) {
      File path = i.next();
      if (output.isFile(path)) {
        children.put(output.getName(path), path);
      }
    }
    assertEquals(2, children.size());
    foo = children.get("foo.txt");
    assertEquals("new_foo_value", output.getContent(foo).getObject().getCharSequence(Charset.defaultCharset()));
    File juu = children.get("juu.txt");
    assertEquals("juu_value", output.getContent(juu).getObject().getCharSequence(Charset.defaultCharset()).toString());
  }

  @Test
  public void testAnnotationState() {
    CaptureAnnotationProcessor processor = new CaptureAnnotationProcessor().with(StringArray.class);
    compiler("compiler.annotationstate.multivalued").with(compilerProvider).with(processor).assertCompile();

    //
    AnnotationState m1 = processor.get(ElementHandle.Method.create("compiler.annotationstate.multivalued.A", "m1"), StringArray.class);
    assertTrue(m1.isUndeclared("value"));
    List<?> value = assertInstanceOf(List.class, m1.safeGet("value"));
    assertNull(m1.get("value"));
    assertEquals(Collections.emptyList(), value);

    //
    AnnotationState m2 = processor.get(ElementHandle.Method.create("compiler.annotationstate.multivalued.A", "m2"), StringArray.class);
    assertTrue(m2.isDeclared("value"));
    value = assertInstanceOf(List.class, m2.safeGet("value"));
    assertSame(value, m2.get("value"));
    assertEquals(Collections.emptyList(), value);

    //
    AnnotationState m3 = processor.get(ElementHandle.Method.create("compiler.annotationstate.multivalued.A", "m3"), StringArray.class);
    assertTrue(m3.isDeclared("value"));
    value = assertInstanceOf(List.class, m3.safeGet("value"));
    assertSame(value, m3.get("value"));
    assertEquals(Arrays.asList("warning_value"), value);

    //
    AnnotationState m4 = processor.get(ElementHandle.Method.create("compiler.annotationstate.multivalued.A", "m4"), StringArray.class);
    assertTrue(m4.isDeclared("value"));
    value = assertInstanceOf(List.class, m4.safeGet("value"));
    assertSame(value, m4.get("value"));
    assertEquals(Arrays.asList("warning_value"), value);

    //
    AnnotationState m5 = processor.get(ElementHandle.Method.create("compiler.annotationstate.multivalued.A", "m5"), StringArray.class);
    assertTrue(m5.isDeclared("value"));
    value = assertInstanceOf(List.class, m5.safeGet("value"));
    assertSame(value, m5.get("value"));
    assertEquals(Arrays.asList("warning_value_1", "warning_value_2"), value);
  }

  @Test
  public void testDot() throws Exception {
    CompilerAssert<File, File> compiler = compiler("compiler.dot").with(compilerProvider);
    compiler.with(new AbstractProcessor() {
      int count = 0;
      @Override
      public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("*");
      }
      @Override
      public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (count++ == 0) {
          try {
            ProcessingContext ctx = new ProcessingContext(processingEnv);
            ElementHandle.Package pkg = ElementHandle.Package.create(ctx.getPackageElement("compiler.dot.foo"));
            FileObject file = ctx.resolveResource(pkg, FileKey.newName("compiler.dot.foo", "a.b.txt"));
            InputStream in = file.openInputStream();
            FileObject o = ctx.createResource(StandardLocation.CLASS_OUTPUT, FileKey.newName("compiler.dot.foo", "a.b.css"));
            OutputStream out = o.openOutputStream();
            Tools.copy(in, out);
            Tools.safeClose(in);
            Tools.safeClose(out);
          }
          catch (Exception e) {
            throw failure(e);
          }
        }
        return true;
      }
    });
    compiler.assertCompile();
    ReadWriteFileSystem<File> classOutput = compiler.getClassOutput();
    File f = new File(classOutput.getRoot(), "compiler/dot/foo/a.b.css");
    InputStream in = new FileInputStream(f);
    String content = Tools.read(in);
    in.close();
    assertEquals("content", content.trim());
  }
}
