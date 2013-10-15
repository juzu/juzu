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

import junit.framework.Assert;
import juzu.impl.compiler.CompilationException;
import juzu.impl.compiler.Compiler;
import juzu.impl.compiler.CompilerConfig;
import juzu.impl.fs.Change;
import juzu.impl.fs.FileSystemScanner;
import juzu.impl.fs.Filter;
import juzu.impl.fs.Snapshot;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.ReadWriteFileSystem;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.ram.RAMFileSystem;

import javax.annotation.processing.Processor;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class CompileStrategy<I, O> {

  /** . */
  final LinkedList<ReadFileSystem<?>> classPath;

  /** . */
  final ReadWriteFileSystem<I> sourcePath;

  /** . */
  final ReadWriteFileSystem<O> sourceOutput;

  /** . */
  final ReadWriteFileSystem<O> classOutput;

  /** . */
  JavaCompilerProvider javaCompiler;

  /** . */
  Provider<? extends Processor> processorFactory;

  /** . */
  final CompilerConfig config;

  public CompileStrategy(
    ReadWriteFileSystem<I> sourcePath,
    ReadWriteFileSystem<O> sourceOutput,
    ReadWriteFileSystem<O> classOutput) {

    //
    this.classPath = new LinkedList<ReadFileSystem<?>>();
    this.sourcePath = sourcePath;
    this.sourceOutput = sourceOutput;
    this.classOutput = classOutput;
    this.config = new CompilerConfig().force(true);
  }

  final Compiler.Builder builder() {
    Compiler.Builder builder = Compiler.builder();
    builder.javaCompiler(javaCompiler);
    builder.processor(processorFactory);
    builder.addClassPath(classPath);
    builder.sourcePath(sourcePath);
    builder.sourceOutput(sourceOutput);
    builder.classOutput(classOutput);
    builder.config(config);
    return builder;
  }

  Compiler compiler;

  abstract void compile() throws IOException, CompilationException;

  void addClassPath(ReadFileSystem<?> classPath) {
    this.classPath.add(classPath);
  }

  /** . */
  private static final Pattern javaFilePattern = Pattern.compile("(.+)\\.java");

  public static class Incremental<I, O> extends CompileStrategy<I, O> {

    /** . */
    final FileSystemScanner<I> scanner;

    /** . */
    private Snapshot<I> snapshot;

    public Incremental(
        ReadWriteFileSystem<I> sourcePath,
        ReadWriteFileSystem<O> sourceOutput,
        ReadWriteFileSystem<O> classOutput) {
      super(sourcePath, sourceOutput, classOutput);

      //
      this.scanner = FileSystemScanner.createHashing(sourcePath);
      this.snapshot  = scanner.take();
    }

    void compile() throws IOException, CompilationException {
      Compiler.Builder builder = builder();

      //
      List<String> toCompile = new ArrayList<String>();
      List<String> toDelete = new ArrayList<String>();

      // Scan the sources
      snapshot = snapshot.scan();
      for (Map.Entry<String, Change> change : snapshot.getChanges().entrySet()) {
        String path = change.getKey();
        if (path.endsWith(".java")) {
          switch (change.getValue()) {
            case REMOVE:
              toDelete.add(path);
              break;
            case ADD:
              toCompile.add(path);
              break;
            case UPDATE:
              toCompile.add(path);
              toDelete.add(path);
              break;
          }
        }
      }

      // Delete the classes corresponding to the deleted classes
      for (String s : toDelete) {
        Matcher matcher = javaFilePattern.matcher(s);
        Assert.assertTrue(matcher.matches());
        String path = matcher.group(1) + ".class";
        String[] names = Tools.split(path, '/');
        O clazz = classOutput.getPath(names);
        if (clazz != null) {
          classOutput.removePath(clazz);
        }
      }

      // Make the current classoutput part of the classpath
      if (classOutput.size(ReadFileSystem.FILE) > 0) {
        File root = File.createTempFile("juzu", "");
        Assert.assertTrue(root.delete());
        Assert.assertTrue(root.mkdirs());
        root.deleteOnExit();
        ReadWriteFileSystem classes = new DiskFileSystem(root);
        classOutput.copy(new Filter.Default<O>() {
          @Override
          public boolean acceptFile(O file, String name) throws IOException {
            return name.endsWith(".class");
          }
        }, classes);
        builder.addClassPath(classes);
      }

      // Add our classpath to the compiler classpath
      for (ReadFileSystem<?> cp : classPath) {
        builder.addClassPath(cp);
      }

      //
      compiler = builder.build();
      compiler.compile(toCompile.toArray(new String[toCompile.size()]));
    }
  }

  public static class Batch<I, O> extends CompileStrategy<I, O> {
    public Batch(
        ReadFileSystem<?> classPath,
        ReadWriteFileSystem<I> sourcePath,
        ReadWriteFileSystem<O> sourceOutput,
        ReadWriteFileSystem<O> classOutput) {
      super(sourcePath, sourceOutput, classOutput);
    }

    void compile() throws IOException, CompilationException {
      compiler = builder().build();
      compiler.compile();
    }
  }
}
