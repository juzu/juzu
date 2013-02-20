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

import junit.framework.Assert;
import juzu.impl.compiler.CompilationException;
import juzu.impl.compiler.Compiler;
import juzu.impl.compiler.CompilerConfig;
import juzu.impl.fs.Change;
import juzu.impl.fs.FileSystemScanner;
import juzu.impl.fs.Snapshot;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.ReadWriteFileSystem;
import juzu.impl.common.Tools;

import javax.annotation.processing.Processor;
import javax.inject.Provider;
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
  final ReadFileSystem<?> classPath;

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
      ReadFileSystem<?> classPath,
    ReadWriteFileSystem<I> sourcePath,
    ReadWriteFileSystem<O> sourceOutput,
    ReadWriteFileSystem<O> classOutput) {
    this.classPath = classPath;
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

  abstract void addClassPath(ReadFileSystem<?> classPath);

  /** . */
  private static final Pattern javaFilePattern = Pattern.compile("(.+)\\.java");

  public static class Incremental<I, O> extends CompileStrategy<I, O> {

    /** . */
    final LinkedList<ReadFileSystem<?>> classPath;

    /** . */
    final FileSystemScanner<I> scanner;

    /** . */
    private Snapshot<I> snapshot;

    public Incremental(
        ReadFileSystem<?> classPath,
        ReadWriteFileSystem<I> sourcePath,
        ReadWriteFileSystem<O> sourceOutput,
        ReadWriteFileSystem<O> classOutput) {
      super(classPath, sourcePath, sourceOutput, classOutput);

      //
      this.classPath = new LinkedList<ReadFileSystem<?>>();
      this.scanner = FileSystemScanner.createHashing(sourcePath);
      this.snapshot  = scanner.take();
    }

    void compile() throws IOException, CompilationException {
      Compiler.Builder builder = builder();

      //
      List<String> toCompile = new ArrayList<String>();
      List<String> toDelete = new ArrayList<String>();

      //
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

      //
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

      //
      for (ReadFileSystem<?> cp : classPath) {
        builder.addClassPath(cp);
      }

      //
      compiler = builder.build();
      compiler.compile(toCompile.toArray(new String[toCompile.size()]));
    }

    @Override
    void addClassPath(ReadFileSystem<?> classPath) {
      this.classPath.add(classPath);
    }
  }

  public static class Batch<I, O> extends CompileStrategy<I, O> {
    public Batch(
        ReadFileSystem<?> classPath,
        ReadWriteFileSystem<I> sourcePath,
        ReadWriteFileSystem<O> sourceOutput,
        ReadWriteFileSystem<O> classOutput) {
      super(classPath, sourcePath, sourceOutput, classOutput);
    }

    void compile() throws IOException, CompilationException {
      compiler = builder().build();
      compiler.compile();
    }

    @Override
    void addClassPath(ReadFileSystem<?> classPath) {
    }
  }
}
