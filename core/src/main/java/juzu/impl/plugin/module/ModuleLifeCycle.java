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

package juzu.impl.plugin.module;

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
public abstract class ModuleLifeCycle<C> {

  /** . */
  protected final Logger logger;

  protected ModuleLifeCycle(Logger logger) {
    this.logger = logger;
  }

  public abstract boolean refresh() throws Exception, CompilationException ;

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

  public static class Dynamic<S> extends ModuleLifeCycle<String[]> {

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
    public boolean refresh() throws Exception, CompilationException {

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

  public static class Static<P> extends ModuleLifeCycle<P> {

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
    public boolean refresh() {
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
