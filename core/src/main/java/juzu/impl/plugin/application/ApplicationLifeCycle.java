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

import juzu.Scope;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetServer;
import juzu.impl.common.Filter;
import juzu.impl.common.Name;
import juzu.impl.common.NameLiteral;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.inject.spi.BeanLifeCycle;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.inject.spi.Injector;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.inject.spi.spring.SpringInjector;
import juzu.impl.common.Logger;
import juzu.impl.plugin.Plugin;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.plugin.asset.AssetPlugin;
import juzu.impl.plugin.module.ModuleLifeCycle;
import juzu.impl.resource.ClassLoaderResolver;
import juzu.impl.resource.ResourceResolver;

import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;

/**
 * The application life cycle.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ApplicationLifeCycle<P, R> implements Closeable {

  /** Configuration: name. */
  private final Name name;

  /** Configuration: injector provider. */
  private final InjectorProvider injectorProvider;

  /** Contextual: logger. */
  private final Logger logger;

  /** Contextual: resources. */
  private final ReadFileSystem<R> resources;

  /** Contextual: resoure resolver. */
  private final ResourceResolver resourceResolver;

  /** Contextual: asset server. */
  private final AssetServer assetServer;

  /** Contextual: module. */
  private final ModuleLifeCycle<P> module;

  /** . */
  private ApplicationDescriptor descriptor;

  /** . */
  private AssetManager stylesheetManager;

  /** . */
  private AssetManager scriptManager;

  /** . */
  private InjectionContext<?, ?> injectionContext;

  /** . */
  private BeanLifeCycle<Application> application;

  public ApplicationLifeCycle(
      Logger logger,
      ModuleLifeCycle<P> module,
      InjectorProvider injectorProvider,
      Name name,
      ReadFileSystem<R> resources,
      AssetServer assetServer,
      ResourceResolver resourceResolver) {

    //
    this.logger = logger;
    this.module = module;
    this.injectorProvider = injectorProvider;
    this.name = name;
    this.resources = resources;
    this.assetServer = assetServer;
    this.resourceResolver = resourceResolver;
  }

  public Name getName() {
    return name;
  }

  public Application getApplication() {
    return application != null ? application.peek() : null;
  }

  public AssetManager getScriptManager() {
    return scriptManager;
  }

  public AssetManager getStylesheetManager() {
    return stylesheetManager;
  }

  public ApplicationDescriptor getDescriptor() {
    return descriptor;
  }

  public ModuleLifeCycle<P> getModule() {
    return module;
  }

  public boolean refresh() throws Exception {
    boolean changed = getModule().refresh();

    //
    if (application != null) {
      if (changed) {
        stop();
      }
    }

    //
    if (application == null) {
      logger.log("Building application");
      start();
      return true;
    } else {
      return false;
    }
  }

  protected final void start() throws Exception {
    ReadFileSystem<P> classes = getModule().getClasses();

    //
    Name fqn = name.append("Application");

    //
    Class<?> clazz = getModule().getClassLoader().loadClass(fqn.toString());
    ApplicationDescriptor descriptor = ApplicationDescriptor.create(clazz);
    //
    Injector injector = injectorProvider.get();
    injector.addFileSystem(classes);
    injector.setClassLoader(getModule().getClassLoader());

    //
    if (injector instanceof SpringInjector) {
      R springName = resources.getPath("spring.xml");
      if (springName != null) {
        URL configurationURL = resources.getURL(springName);
        ((SpringInjector)injector).setConfigurationURL(configurationURL);
      }
    }

    // Bind the resolver
    ClassLoaderResolver resolver = new ClassLoaderResolver(getModule().getClassLoader());
    injector.bindBean(ResourceResolver.class, Collections.<Annotation>singletonList(new NameLiteral("juzu.resource_resolver.classpath")), resolver);
    injector.bindBean(ResourceResolver.class, Collections.<Annotation>singletonList(new NameLiteral("juzu.resource_resolver.server")), this.resourceResolver);

    //
    logger.log("Starting " + descriptor.getName());
    InjectionContext<?, ?> injectionContext = doStart(descriptor, injector);

    //
    AssetPlugin assetPlugin = injectionContext.get(AssetPlugin.class).get();
    BeanLifeCycle<Application> application = injectionContext.get(Application.class);

    //
    if (assetServer != null) {
      assetServer.register(this);
    }

    //
    this.injectionContext = injectionContext;
    this.scriptManager = assetPlugin.getScriptManager();
    this.stylesheetManager = assetPlugin.getStylesheetManager();
    this.descriptor = descriptor;
    this.application = application;

    // For application start (perhaps we could remove that)
    try {
      application.get();
    }
    catch (InvocationTargetException e) {
      throw new UnsupportedOperationException("handle me gracefully", e);
    }
  }

  private <B, I> InjectionContext<B, I> doStart(final ApplicationDescriptor descriptor, Injector injector) {

    // Bind the application descriptor
    injector.bindBean(ApplicationDescriptor.class, null, descriptor);

    // Bind the application context
    injector.declareBean(Application.class, null, null, null);

    // Bind the scopes
    for (Scope scope : Scope.values()) {
      injector.addScope(scope);
    }

    // Bind the plugins
    for (Plugin plugin : descriptor.getPlugins().values()) {
      Class aClass = plugin.getClass();
      Object o = plugin;
      injector.bindBean(aClass, null, o);
    }

    // Bind the beans
    for (BeanDescriptor bean : descriptor.getBeans()) {
      bean.bind(injector);
    }

    // Filter the classes:
    // any class beginning with juzu. is refused
    // any class prefixed with the application package is accepted
    // any other application class is refused (i.e a class having an ancestor package annotated with @Application)
    Filter<Class<?>> filter = new Filter<Class<?>>() {
      HashSet<String> blackList = new HashSet<String>();
      public boolean accept(Class<?> elt) {
        if (elt.getName().startsWith("juzu.")) {
          return false;
        } else if (elt.getPackage().getName().startsWith(descriptor.getPackageName())) {
          return true;
        } else {
          for (String currentPkg = elt.getPackage().getName();currentPkg != null;currentPkg = Tools.parentPackageOf(currentPkg)) {
            if (blackList.contains(currentPkg)) {
              return false;
            } else {
              try {
                Class<?> packageClass = descriptor.getApplicationLoader().loadClass(currentPkg + ".package-info");
                juzu.Application ann = packageClass.getAnnotation(juzu.Application.class);
                if (ann != null) {
                  blackList.add(currentPkg);
                  return false;
                }
              }
              catch (ClassNotFoundException e) {
                // Skip it
              }
            }
          }
          return true;
        }
      }
    };

    //
    InjectionContext<B, I> injectionContext;
    try {
      injectionContext = (InjectionContext<B, I>)injector.create(filter);
    }
    catch (Exception e) {
      throw new UnsupportedOperationException("handle me gracefully", e);
    }

    //
    return injectionContext;
  }

  void stop() {
    if (assetServer != null) {
      assetServer.unregister(this);
    }
    Tools.safeClose(application);
    Tools.safeClose(injectionContext);
    application = null;
    injectionContext = null;
    stylesheetManager = null;
    scriptManager = null;
    descriptor = null;
  }

  public void close() {
    stop();
  }
}
