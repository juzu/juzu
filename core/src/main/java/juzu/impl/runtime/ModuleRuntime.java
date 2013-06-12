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

package juzu.impl.runtime;

import juzu.impl.common.DevClassLoader;
import juzu.impl.common.Logger;
import juzu.impl.compiler.*;
import juzu.impl.compiler.Compiler;
import juzu.impl.fs.FileSystemScanner;
import juzu.impl.fs.Filter;
import juzu.impl.fs.Snapshot;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.ram.RAMFileSystem;
import juzu.impl.fs.spi.url.URLFileSystem;
import juzu.processor.MainProcessor;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * The module life cycle.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class ModuleRuntime<C> {

  /** . */
  protected final Logger logger;

  protected ModuleRuntime(Logger logger) {
    this.logger = logger;
  }

  /**
   * Refresh the module.
   *
   * @param recompile true if recompilation can occur
   * @return true when the refresh operation has triggered changes
   * @throws Exception any exception
   * @throws CompilationException any compilation exception
   */
  public abstract boolean refresh(boolean recompile) throws Exception, CompilationException ;

  /**
   * Returns the module classloader.
   *
   * @return the module classloader
   */
  public abstract ClassLoader getClassLoader();

  /**
   * Returns the module classes.
   *
   * @return the module classes
   */
  public abstract ReadFileSystem<C> getClasses();

  public static class Dynamic<S> extends ModuleRuntime<String[]> {

    /** . */
    private final ClassLoader baseClassLoader;

    /** . */
    private URLFileSystem classPath;

    /** . */
    private FileSystemScanner<S> scanner;

    /** . */
    private Snapshot<S> snapshot;

    /** . */
    private ClassLoader classLoader;

    /** . */
    private ClassLoader devClassLoader;

    /** . */
    private ReadFileSystem<String[]> classes;

    /** . */
    private boolean failed;

    public Dynamic(Logger logger, ClassLoader baseClassLoader, ReadFileSystem<S> source) {
      super(logger);

      //
      this.classLoader = null;
      this.baseClassLoader = baseClassLoader;
      this.devClassLoader = new DevClassLoader(baseClassLoader);
      this.scanner = FileSystemScanner.createHashing(source);
      this.snapshot = scanner.take();
      this.classPath = null;
      this.failed = false;
    }

    @Override
    public boolean refresh(boolean recompile) throws Exception, CompilationException {

      //
      if (!recompile) {
        throw new UnsupportedOperationException("Not yet implemented");
      }

      // Lazy initialize
      if (classPath == null) {
        classPath = new URLFileSystem().add(devClassLoader, ClassLoader.getSystemClassLoader().getParent());
      }

      Snapshot<S> next = snapshot.scan();

      //
      if (!failed && !next.hasChanges()) {
        logger.log("No changes detected");
        return false;
      }
      else {
        logger.log("Building application");

        //
        this.failed = true;

        //
        ReadFileSystem<S> sourcePath = scanner.getFileSystem();

        // Copy everything that is not a java source
        RAMFileSystem classOutput = new RAMFileSystem();
        sourcePath.copy(new Filter.Default() {
          @Override
          public boolean acceptFile(Object file, String name) throws IOException {
            return !name.endsWith(".java");
          }
        }, classOutput);

        //
        Compiler compiler = Compiler.
            builder().
            sourcePath(sourcePath).
            sourceOutput(classOutput).
            classOutput(classOutput).
            addClassPath(classPath).build();
        compiler.addAnnotationProcessor(new MainProcessor());
        compiler.compile();

        //
        this.classLoader = new URLClassLoader(new URL[]{classOutput.getURL()}, devClassLoader);
        this.classes = classOutput;
        this.snapshot = next;
        this.failed = false;

        //
        return true;
      }
    }

    @Override
    public ClassLoader getClassLoader() {
      return classLoader;
    }

    @Override
    public ReadFileSystem<String[]> getClasses() {
      return classes;
    }
  }

  public static class Static<P> extends ModuleRuntime<P> {

    /** . */
    private final ClassLoader classLoader;

    /** . */
    private final ReadFileSystem<P> classes;

    public Static(Logger logger, ClassLoader classLoader, ReadFileSystem<P> classes) {
      super(logger);

      //
      this.classLoader = classLoader;
      this.classes = classes;
    }

    @Override
    public boolean refresh(boolean recompile) {
      return false;
    }

    @Override
    public ClassLoader getClassLoader() {
      return classLoader;
    }

    @Override
    public ReadFileSystem<P> getClasses() {
      return classes;
    }
  }
}
