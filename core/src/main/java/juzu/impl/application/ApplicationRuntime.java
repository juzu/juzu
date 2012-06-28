/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package juzu.impl.application;

import juzu.asset.AssetType;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetServer;
import juzu.impl.asset.ManagerQualifier;
import juzu.impl.compiler.CompilationError;
import juzu.impl.compiler.Compiler;
import juzu.impl.fs.Change;
import juzu.impl.fs.FileSystemScanner;
import juzu.impl.fs.Filter;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.metadata.Descriptor;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.classloader.ClassLoaderFileSystem;
import juzu.impl.fs.spi.jar.JarFileSystem;
import juzu.impl.fs.spi.ram.RAMFileSystem;
import juzu.impl.fs.spi.ram.RAMPath;
import juzu.impl.inject.spi.InjectBuilder;
import juzu.impl.inject.spi.InjectImplementation;
import juzu.impl.inject.spi.spring.SpringBuilder;
import juzu.impl.utils.JSON;
import juzu.impl.utils.Logger;
import juzu.impl.utils.Tools;
import juzu.processor.MainProcessor;

import javax.portlet.PortletException;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * The application runtime.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class ApplicationRuntime<P, R, L> {

  /** . */
  private static final String[] CONFIG_PATH = {"juzu", "config.json"};

  /** . */
  protected final Logger logger;

  /** . */
  protected ReadFileSystem<L> libs;

  /** . */
  protected String name;

  /** . */
  protected InjectImplementation injectImplementation;

  /** . */
  protected ReadFileSystem<R> resources;

  /** . */
  protected ApplicationContext context;

  /** . */
  protected AssetServer assetServer;

  /** . */
  protected AssetManager stylesheetManager;

  /** . */
  protected AssetManager scriptManager;

  /** Additional plugins. */
  protected Map<String, Descriptor> plugins;

  ApplicationRuntime(Logger logger) {
    this.logger = logger;
  }

  public Logger getLogger() {
    return logger;
  }

  public ReadFileSystem<L> getLibs() {
    return libs;
  }

  public void setLibs(ReadFileSystem<L> libs) {
    this.libs = libs;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public InjectImplementation getInjectImplementation() {
    return injectImplementation;
  }

  public void setInjectImplementation(InjectImplementation injectImplementation) {
    this.injectImplementation = injectImplementation;
  }

  public ReadFileSystem<R> getResources() {
    return resources;
  }

  public void setResources(ReadFileSystem<R> resources) {
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

  public void addPlugin(String name, Descriptor plugin) {
    if (plugins == null) {
      plugins = new HashMap<String, Descriptor>();
    }
    plugins.put(name, plugin);
  }

  public abstract ClassLoader getClassLoader();

  protected abstract ReadFileSystem<P> getClasses();

  public abstract Collection<CompilationError> boot() throws Exception;

  public static class Static<P, R, L> extends ApplicationRuntime<P, R, L> {

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
    public Collection<CompilationError> boot() throws Exception {
      if (context == null) {
        doBoot();
      }

      //
      return null;
    }
  }

  public static class Dynamic<R, L, S> extends ApplicationRuntime<RAMPath, R, L> {

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

    public Collection<CompilationError> boot() throws Exception {
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
        List<CompilationError> res = compiler.compile();
        if (res.isEmpty()) {
          this.classLoader = new URLClassLoader(new URL[]{classOutput.getURL()}, baseClassLoader);
          this.classes = classOutput;

          //
          doBoot();

          // Return empty to signal compilation occured
          return Collections.emptyList();
        }
        else {
          return res;
        }
      }
      else {
        return null;
      }
    }

    @Override
    public ClassLoader getClassLoader() {
      return classLoader;
    }
  }

  protected final void doBoot() throws Exception {
    List<URL> jarURLs = new ArrayList<URL>();
    for (Iterator<L> i = libs.getChildren(libs.getRoot());i.hasNext();) {
      L s = i.next();
      URL url = libs.getURL(s);
      jarURLs.add(url);
    }

    // Find an application
    P f = getClasses().getPath(CONFIG_PATH);
    URL url = getClasses().getURL(f);
    String s = Tools.read(url);
    JSON json = (JSON)JSON.parse(s);

    // Get the application name
    String fqn = null;
    if (name != null) {
      fqn = (String)json.get(name.trim());
    }
    else {
      // Find the first valid application for now
      for (String a : json.names()) {
        String b = json.getString(a);
        if (a.length() > 0 && b.length() > 0) {
          fqn = b;
          break;
        }
      }
    }

    //
    if (fqn == null) {
      throw new Exception("Could not find an application to start " + json);
    }

    //
    Class<?> clazz = getClassLoader().loadClass(fqn);
    Field field = clazz.getDeclaredField("DESCRIPTOR");
    ApplicationDescriptor descriptor = (ApplicationDescriptor)field.get(null);

    // Add additional plugins when available
    if (plugins != null) {
      for (Map.Entry<String, Descriptor> entry : plugins.entrySet()) {
        descriptor.addPlugin(entry.getKey(), entry.getValue());
      }
    }

    // Find the juzu jar
    URL mainURL = ApplicationBootstrap.class.getProtectionDomain().getCodeSource().getLocation();
    if (mainURL == null) {
      throw new PortletException("Cannot find juzu jar among " + jarURLs);
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
    injectBootstrap.addFileSystem(getClasses());
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
  }

  public void shutdown() {
  }
}
