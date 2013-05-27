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

package juzu.impl.plugin.application;

import juzu.Scope;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetServer;
import juzu.impl.common.Filter;
import juzu.impl.common.JSON;
import juzu.impl.common.Name;
import juzu.impl.plugin.PluginDescriptor;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.inject.spi.BeanLifeCycle;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.inject.spi.Injector;
import juzu.impl.inject.spi.spring.SpringInjector;
import juzu.impl.common.Logger;
import juzu.impl.plugin.Plugin;
import juzu.impl.plugin.PluginContext;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.plugin.asset.AssetPlugin;
import juzu.impl.plugin.module.ModuleLifeCycle;
import juzu.impl.resource.ResourceResolver;

import javax.inject.Provider;
import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 * The application life cycle.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ApplicationLifeCycle<P, R> implements Closeable {

  /** Configuration: name. */
  private final Name name;

  /** Configuration: injector provider. */
  private final Provider<Injector> injectorProvider;

  /** Contextual: logger. */
  private final Logger log;

  /** Contextual: resources. */
  private final ReadFileSystem<R> resources;

  /** Contextual: resoure resolver. */
  private final ResourceResolver<URL> resourceResolver;

  /** Contextual: asset server. */
  private final AssetServer assetServer;

  /** Contextual: module. */
  private final ModuleLifeCycle<?> moduleLifeCycle;

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

  /** The last used class loader : used for checking refresh. */
  private ClassLoader classLoader;

  /** . */
  private Map<String, ApplicationPlugin> plugins;

  /** . */
  private Map<String, PluginDescriptor> pluginDescriptors;

  public ApplicationLifeCycle(
      Logger log,
      ModuleLifeCycle<?> moduleLifeCycle,
      Provider<Injector> injectorProvider,
      Name name,
      ReadFileSystem<R> resources,
      AssetServer assetServer,
      ResourceResolver<URL> resourceResolver) {

    //
    this.log = log;
    this.moduleLifeCycle = moduleLifeCycle;
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

  public <T> T resolveBean(Class<T> beanType) {
    try {
      BeanLifeCycle<T> pluginLifeCycle = injectionContext.get(beanType);
      return pluginLifeCycle != null ? pluginLifeCycle.get() : null;
    }
    catch (InvocationTargetException e) {
      log.log("Could not retrieve bean of type " + beanType, e.getCause());
      return null;
    }
  }

  public <T> Iterable<T> resolveBeans(final Class<T> beanType) {
    return new Iterable<T>() {
      Iterable<BeanLifeCycle<T>> lifecycles = injectionContext.resolve(beanType);
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          Iterator<BeanLifeCycle<T>> iterator = lifecycles.iterator();
          T next = null;
          public boolean hasNext() {
            while (next == null && iterator.hasNext()) {
              try {
                BeanLifeCycle<T> pluginLifeCycle = iterator.next();
                next = pluginLifeCycle.get();
              }
              catch (InvocationTargetException e) {
                log.log("Could not retrieve bean of type " + beanType.getName(), e);
              }
            }
            return next != null;
          }
          public T next() {
            if (!hasNext()) {
              throw new NoSuchElementException();
            } else {
              T tmp = next;
              next = null;
              return tmp;
            }
          }
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public boolean refresh() throws Exception {
    if (application != null) {
      if (classLoader != moduleLifeCycle.getClassLoader()) {
        stop();
      }
    }

    //
    if (application == null) {
      log.log("Building application");
      start();
      return true;
    } else {
      return false;
    }
  }

  protected final void start() throws Exception {
    ReadFileSystem<?> classes = moduleLifeCycle.getClasses();

    //
    Name fqn = name.append("Application");

    //
    Class<?> clazz = moduleLifeCycle.getClassLoader().loadClass(fqn.toString());
    ApplicationDescriptor descriptor = ApplicationDescriptor.create(clazz);

    // Take care of plugins
    HashMap<String, ApplicationPlugin> plugins = new HashMap<String, ApplicationPlugin>();
    for (ApplicationPlugin plugin : ServiceLoader.load(ApplicationPlugin.class)) {
      plugins.put(plugin.getName(), plugin);
    }
    HashSet<String> names = new HashSet<String>(descriptor.getConfig().names());
    HashMap<ApplicationPlugin, JSON> configs = new HashMap<ApplicationPlugin, JSON>();
    for (ApplicationPlugin plugin : plugins.values()) {
      String name = plugin.getName();
      if (names.remove(name)) {
        configs.put(plugin, descriptor.getConfig().getJSON(plugin.getName()));
      } else {
        configs.put(plugin, null);
      }
    }
    if (names.size() > 0) {
      throw new UnsupportedOperationException("Handle me gracefully : missing plugins " + names);
    }

    //
    final ResourceResolver<URL> applicationResolver = new ResourceResolver<URL>() {
      public URL resolve(String uri) {
        if (uri.startsWith("/")) {
          return moduleLifeCycle.getClassLoader().getResource(uri.substring(1));
        } else {
          return null;
        }
      }
    };

    //
    HashMap<String, PluginDescriptor> pluginDescriptors = new HashMap<String, PluginDescriptor>();
    for (final Map.Entry<ApplicationPlugin, JSON> entry : configs.entrySet()) {
      ApplicationPlugin plugin = entry.getKey();
      PluginContext pluginContext = new PluginContext() {
        public JSON getConfig() {
          return entry.getValue();
        }
        public ClassLoader getClassLoader() {
          return moduleLifeCycle.getClassLoader();
        }
        public ResourceResolver<URL> getServerResolver() {
          return resourceResolver;
        }
        public ResourceResolver<URL> getApplicationResolver() {
          return applicationResolver;
        }
      };
      plugin.setApplication(descriptor);
      PluginDescriptor pluginDescriptor = plugin.init(pluginContext);
      if (pluginDescriptor != null) {
        pluginDescriptors.put(plugin.getName(), pluginDescriptor);
      }
    }

    //
    for (Iterator<String> i = plugins.keySet().iterator();i.hasNext();) {
      String name = i.next();
      if (!pluginDescriptors.containsKey(name)) {
        i.remove();
      }
    }

    //
    Injector injector = injectorProvider.get();
    injector.addFileSystem(classes);
    injector.setClassLoader(moduleLifeCycle.getClassLoader());

    //
    if (injector instanceof SpringInjector) {
      R springName = resources.getPath("spring.xml");
      if (springName != null) {
        URL configurationURL = resources.getURL(springName);
        ((SpringInjector)injector).setConfigurationURL(configurationURL);
      }
    }

    //
    log.log("Starting " + descriptor.getName());
    InjectionContext<?, ?> injectionContext = doStart(descriptor, injector, plugins.values(), pluginDescriptors.values());

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
    this.classLoader = moduleLifeCycle.getClassLoader();
    this.plugins = plugins;
    this.pluginDescriptors = pluginDescriptors;

    // For application start (perhaps we could remove that)
    try {
      application.get();
    }
    catch (InvocationTargetException e) {
      throw new UnsupportedOperationException("handle me gracefully", e);
    }
  }

  private static <B, I> InjectionContext<B, I> doStart(
      final ApplicationDescriptor descriptor,
      Injector injector,
      Collection<ApplicationPlugin> plugins,
      Collection<PluginDescriptor> pluginDescriptors) {

    // Bind the application descriptor
    injector.bindBean(ApplicationDescriptor.class, null, descriptor);

    // Bind the application context
    injector.declareBean(Application.class, null, null, null);

    // Bind the scopes
    for (Scope scope : Scope.values()) {
      injector.addScope(scope);
    }

    // Bind the plugins
    for (Plugin plugin : plugins) {

      // Bind the plugin as a bean
      Class aClass = plugin.getClass();
      Object o = plugin;
      injector.bindBean(aClass, null, o);
    }

    // Bind the beans
    for (PluginDescriptor pluginDescriptor : pluginDescriptors) {
      for (BeanDescriptor bean : pluginDescriptor.getBeans()) {
        bean.bind(injector);
      }
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
    classLoader = null;
    plugins = null;
    pluginDescriptors = null;
  }

  public void close() {
    stop();
  }
}
