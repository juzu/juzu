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

package juzu.impl.plugin.application;

import juzu.asset.AssetType;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetServer;
import juzu.impl.asset.ManagerQualifier;
import juzu.impl.common.FQN;
import juzu.impl.common.QN;
import juzu.impl.compiler.CompilationException;
import juzu.impl.compiler.Compiler;
import juzu.impl.fs.Change;
import juzu.impl.fs.FileSystemScanner;
import juzu.impl.fs.Filter;
import juzu.impl.fs.spi.SimpleFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.classloader.ClassLoaderFileSystem;
import juzu.impl.fs.spi.jar.JarFileSystem;
import juzu.impl.fs.spi.ram.RAMFileSystem;
import juzu.impl.fs.spi.ram.RAMPath;
import juzu.impl.inject.spi.InjectBuilder;
import juzu.impl.inject.spi.InjectImplementation;
import juzu.impl.inject.spi.spring.SpringBuilder;
import juzu.impl.common.Logger;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.processor.MainProcessor;

import javax.portlet.PortletException;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * The application runtime.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class ApplicationRuntime<P, R> {

  /** . */
  protected final Logger logger;

  /** . */
  protected QN name;

  /** . */
  protected InjectImplementation injectImplementation;

  /** . */
  protected SimpleFileSystem<R> resources;

  /** . */
  protected ApplicationContext context;

  /** . */
  protected AssetServer assetServer;

  /** . */
  protected AssetManager stylesheetManager;

  /** . */
  protected AssetManager scriptManager;

  /** . */
  protected ApplicationDescriptor descriptor;

  /** . */
  protected ApplicationBootstrap bootstrap;

  ApplicationRuntime(Logger logger) {
    this.logger = logger;
  }

  public Logger getLogger() {
    return logger;
  }

  public QN getName() {
    return name;
  }

  public void setName(QN name) {
    this.name = name;
  }

  public InjectImplementation getInjectImplementation() {
    return injectImplementation;
  }

  public void setInjectImplementation(InjectImplementation injectImplementation) {
    this.injectImplementation = injectImplementation;
  }

  public SimpleFileSystem<R> getResources() {
    return resources;
  }

  public void setResources(SimpleFileSystem<R> resources) {
    this.resources = resources;
  }

  public ApplicationContext getContext() {
    return context;
  }

  public AssetServer getAssetServer() {
    return assetServer;
  }

  public AssetManager getScriptManager() {
    return scriptManager;
  }

  public AssetManager getStylesheetManager() {
    return stylesheetManager;
  }

  public void setAssetServer(AssetServer assetServer) {
    if (assetServer != null) {
      assetServer.register(this);
    }
    if (this.assetServer != null) {
      this.assetServer.unregister(this);
    }
    this.assetServer = assetServer;
  }

  public ApplicationDescriptor getDescriptor() {
    return descriptor;
  }

  public abstract ClassLoader getClassLoader();

  protected abstract ReadFileSystem<P> getClasses();

  public abstract void boot() throws Exception, CompilationException;

  public static class Static<P, R> extends ApplicationRuntime<P, R> {

    /** . */
    private ReadFileSystem<P> classes;

    /** . */
    private ClassLoader classLoader;

    public Static(Logger logger) {
      super(logger);
    }

    public ReadFileSystem<P> getClasses() {
      return classes;
    }

    public void setClasses(ReadFileSystem<P> classes) {
      this.classes = classes;
    }

    @Override
    public ClassLoader getClassLoader() {
      return classLoader;
    }

    public void setClassLoader(ClassLoader cl) {
      this.classLoader = cl;
    }

    @Override
    public void boot() throws Exception {
      if (context == null) {
        doBoot();
      }
    }
  }

  public static class Dynamic<R, S> extends ApplicationRuntime<RAMPath, R> {

    /** . */
    private FileSystemScanner<S> devScanner;

    /** . */
    private ClassLoaderFileSystem classLoaderFS;

    /** . */
    private ReadFileSystem<RAMPath> classes;

    /** . */
    private ClassLoader classLoader;

    /** . */
    private ClassLoader baseClassLoader;

    public Dynamic(Logger logger) {
      super(logger);
    }

    public void init(ClassLoader baseClassLoader, ReadFileSystem<S> fss) throws Exception {
      devScanner = FileSystemScanner.createTimestamped(fss);
      devScanner.scan();
      logger.log("Dev mode scanner monitoring " + fss.getFile(fss.getRoot()));

      //
      this.baseClassLoader = baseClassLoader;
      this.classLoaderFS = new ClassLoaderFileSystem(baseClassLoader);
    }

    public void init(ClassLoaderFileSystem baseClassPath, ReadFileSystem<S> fss) throws Exception {
      devScanner = FileSystemScanner.createTimestamped(fss);
      devScanner.scan();
      logger.log("Dev mode scanner monitoring " + fss.getFile(fss.getRoot()));

      //
      this.baseClassLoader = baseClassPath.getClassLoader();
      this.classLoaderFS = baseClassPath;
    }

    @Override
    protected ReadFileSystem<RAMPath> getClasses() {
      return classes;
    }

    public void boot() throws Exception {
      Map<String, Change> changes = devScanner.scan();
      if (context != null) {
        if (changes.size() > 0) {
          logger.log("Detected changes : " + changes);
          context = null;
        }
        else {
          logger.log("No changes detected");
        }
      }

      //
      if (context == null) {
        logger.log("Building application");

        //
        ReadFileSystem<S> sourcePath = devScanner.getFileSystem();

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
          addClassPath(classLoaderFS).build();
        compiler.addAnnotationProcessor(new MainProcessor());
        compiler.compile();
        this.classLoader = new URLClassLoader(new URL[]{classOutput.getURL()}, baseClassLoader);
        this.classes = classOutput;
        doBoot();
      }
    }

    @Override
    public ClassLoader getClassLoader() {
      return classLoader;
    }
  }

  public static class Provided<R, S> extends ApplicationRuntime<R, S> {

    public static void set(ApplicationRuntime<?, ?> runtime) {
      System.getProperties().put("abc", runtime);
    }

    private ApplicationRuntime<?, ?> get() {
      return (ApplicationRuntime<?, ?>)System.getProperties().get("abc");
    }

    public Provided(Logger logger) {
      super(logger);
    }

    @Override
    public ClassLoader getClassLoader() {
      return get().getClassLoader();
    }

    @Override
    protected ReadFileSystem<R> getClasses() {
      return (ReadFileSystem<R>)get().getClasses();
    }

    @Override
    public void boot() throws Exception {
      if (context == null) {

        // Get delegate
        ApplicationRuntime delegate = get();

        // Boot it
        delegate.boot();

        //
        this.context = delegate.getContext();
        this.scriptManager = delegate.scriptManager;
        this.stylesheetManager = delegate.stylesheetManager;
        this.descriptor = delegate.descriptor;
        this.bootstrap = delegate.bootstrap;
      }
    }
  }

  protected final void doBoot() throws Exception {
    ReadFileSystem<P> classes = getClasses();

    //
    FQN fqn = new FQN(name, "Application");

    //
    Class<?> clazz = getClassLoader().loadClass(fqn.toString());
    Field field = clazz.getDeclaredField("DESCRIPTOR");
    ApplicationDescriptor descriptor = (ApplicationDescriptor)field.get(null);

    // Find the juzu jar
    URL mainURL = ApplicationBootstrap.class.getProtectionDomain().getCodeSource().getLocation();
    if (mainURL == null) {
      throw new PortletException("Cannot find juzu jar");
    }
    if (!mainURL.getProtocol().equals("file")) {
      throw new PortletException("Cannot handle " + mainURL);
    }
    File file = new File(mainURL.toURI());
    ReadFileSystem<?> libs;
    if (file.isDirectory()) {
      libs = new DiskFileSystem(file);
    } else {
      libs = new JarFileSystem(new JarFile(file));
    }

    //
    InjectBuilder injectBootstrap = injectImplementation.builder();
    injectBootstrap.addFileSystem(classes);
    injectBootstrap.addFileSystem(libs);
    injectBootstrap.setClassLoader(getClassLoader());

    //
    if (injectBootstrap instanceof SpringBuilder) {
      R springName = resources.getPath("spring.xml");
      if (springName != null) {
        URL configurationURL = resources.getURL(springName);
        ((SpringBuilder)injectBootstrap).setConfigurationURL(configurationURL);
      }
    }

    //
    ApplicationBootstrap bootstrap = new ApplicationBootstrap(
      injectBootstrap,
      descriptor
    );

    //
    AssetManager scriptManager = new AssetManager();
    AssetManager stylesheetManager = new AssetManager();
    injectBootstrap.bindBean(
      AssetManager.class,
      Collections.<Annotation>singletonList(new ManagerQualifier(AssetType.SCRIPT)),
      scriptManager);
    injectBootstrap.bindBean(
      AssetManager.class,
      Collections.<Annotation>singletonList(new ManagerQualifier(AssetType.STYLESHEET)),
      stylesheetManager);

    //
    //
    logger.log("Starting " + descriptor.getName());
    bootstrap.start();

    //
    this.context = bootstrap.getContext();
    this.scriptManager = scriptManager;
    this.stylesheetManager = stylesheetManager;
    this.descriptor = descriptor;
    this.bootstrap = bootstrap;
  }

  public void shutdown() {
    if (bootstrap != null) {
      bootstrap.stop();
    }
  }
}
