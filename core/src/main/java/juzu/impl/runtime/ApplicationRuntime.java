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

import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetServer;
import juzu.impl.common.Completion;
import juzu.impl.common.Name;
import juzu.impl.common.RunMode;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.inject.spi.BeanLifeCycle;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.inject.spi.Injector;
import juzu.impl.common.Logger;
import juzu.impl.plugin.application.Application;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.plugin.asset.AssetService;
import juzu.impl.resource.ResourceResolver;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;

/**
 * The application life cycle.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ApplicationRuntime<P, R> implements Closeable {

  /** Configuration: name. */
  private final Name name;

  /** Configuration: injector provider. */
  private final Injector injectorProvider;

  /** Contextual: logger. */
  private final Logger log;

  /** Contextual: resoure resolver. */
  private final ResourceResolver resourceResolver;

  /** Contextual: asset server. */
  private final AssetServer assetServer;

  /** Contextual: module. */
  private final ModuleRuntime<?> moduleLifeCycle;

  /** . */
  private ApplicationDescriptor descriptor;

  /** . */
  private AssetManager assetManager;

  /** . */
  private InjectionContext<?, ?> injectionContext;

  /** . */
  private Application application;

  /** The last used class loader : used for checking refresh. */
  private ClassLoader classLoader;

  /** . */
  private final RunMode runMode;

  public ApplicationRuntime(
      Logger log,
      RunMode runMode,
      ModuleRuntime<?> moduleLifeCycle,
      Injector injectorProvider,
      Name name,
      AssetServer assetServer,
      ResourceResolver resourceResolver) {

    //
    this.log = log;
    this.moduleLifeCycle = moduleLifeCycle;
    this.injectorProvider = injectorProvider;
    this.name = name;
    this.assetServer = assetServer;
    this.resourceResolver = resourceResolver;
    this.runMode = runMode;
  }

  public Name getName() {
    return name;
  }

  public Application getApplication() {
    return application != null ? application : null;
  }

  public AssetManager getAssetManager() {
    return assetManager;
  }

  public ApplicationDescriptor getDescriptor() {
    return descriptor;
  }

  public <T> T resolveBean(Class<T> beanType) {
    return injectionContext.resolveInstance(beanType);
  }

  public <T> Iterable<T> resolveBeans(final Class<T> beanType) {
    return injectionContext.resolveInstances(beanType);
  }

  public synchronized Completion<Boolean> refresh() {
    if (application != null) {
      if (classLoader != moduleLifeCycle.getClassLoader()) {
        stop();
      }
    }

    //
    if (application == null) {
      try {
        start();
        return Completion.completed(true);
      }
      catch (Exception e) {
        return Completion.failed(e);
      }
    } else {
      return Completion.completed(false);
    }
  }

  protected final void start() throws Exception {
    ReadFileSystem<?> classes = moduleLifeCycle.getClasses();

    //
    ApplicationDescriptor descriptor = ApplicationDescriptor.create(moduleLifeCycle.getClassLoader(), name.toString());

    //
    Injector injector = injectorProvider.get();
    injector.addFileSystem(classes);
    injector.setClassLoader(moduleLifeCycle.getClassLoader());

    //
    log.info("Starting " + descriptor.getName());
    Application application = new Application(injector, descriptor, resourceResolver);
    application.start();
    InjectionContext<?, ?> injectionContext = application.getInjectionContext();

    //
    AssetService assetPlugin = injectionContext.get(AssetService.class).get();

    //
    if (assetServer != null) {

      assetServer.register(application, runMode.getCacheAssets());
    }

    //
    this.injectionContext = injectionContext;
    this.assetManager = assetPlugin.getAssetManager();
    this.descriptor = descriptor;
    this.application = application;
    this.classLoader = moduleLifeCycle.getClassLoader();

    // For application start (perhaps we could remove that)
    BeanLifeCycle lf = injectionContext.get(Application.class);
    try {
      lf.get();
    }
    catch (InvocationTargetException e) {
      throw new UnsupportedOperationException("handle me gracefully", e);
    }
    finally {
      lf.close();
    }
  }

  void stop() {
    if (assetServer != null) {
      assetServer.unregister(application);
    }
    Tools.safeClose(injectionContext);
    application = null;
    injectionContext = null;
    assetManager = null;
    descriptor = null;
    classLoader = null;
  }

  public void close() {
    stop();
  }
}
