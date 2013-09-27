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
import juzu.impl.common.Filter;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.inject.spi.Injector;
import juzu.impl.plugin.Plugin;
import juzu.impl.plugin.PluginContext;
import juzu.impl.plugin.PluginDescriptor;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.resource.ResourceResolver;

import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Singleton
public class Application {

  /** . */
  private final ApplicationDescriptor descriptor;

  /** . */
  InjectionContext<?, ?> injectionContext;

  /** . */
  final ResourceResolver resourceResolver;

  /** . */
  final Injector injector;

  /** . */
  private Map<String, ApplicationPlugin> plugins;

  public Application(Injector injector, ApplicationDescriptor descriptor, ResourceResolver resourceResolver) {
    this.injectionContext = null;
    this.descriptor = descriptor;
    this.injector = injector;
    this.resourceResolver = resourceResolver;
    this.plugins = Collections.emptyMap();
  }

  public void start() throws Exception {

    final ResourceResolver applicationResolver = new ResourceResolver() {
      public URL resolve(String uri) {
        if (uri.startsWith("/")) {
          return descriptor.getApplicationLoader().getResource(uri.substring(1));
        } else {
          return null;
        }
      }
    };

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
    HashMap<String, PluginDescriptor> pluginDescriptors = new HashMap<String, PluginDescriptor>();
    for (final Map.Entry<ApplicationPlugin, JSON> entry : configs.entrySet()) {
      ApplicationPlugin plugin = entry.getKey();
      PluginContext pluginContext = new PluginContext() {
        public JSON getConfig() {
          return entry.getValue();
        }
        public ClassLoader getClassLoader() {
          return descriptor.getApplicationLoader();
        }
        public ResourceResolver getServerResolver() {
          return resourceResolver;
        }
        public ResourceResolver getApplicationResolver() {
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

    // Bind the plugins
    for (Plugin plugin : plugins.values()) {

      // Bind the plugin as a bean
      Class aClass = plugin.getClass();
      Object o = plugin;
      injector.bindBean(aClass, null, o);
    }

    // Bind the beans
    for (PluginDescriptor pluginDescriptor : pluginDescriptors.values()) {
      for (BeanDescriptor bean : pluginDescriptor.getBeans()) {
        bean.bind(injector);
      }
    }

    // Bind the application descriptor
    injector.bindBean(ApplicationDescriptor.class, null, descriptor);

    // Bind ourself
    injector.bindBean(Application.class, null, this);

    // Bind the scopes
    for (Scope scope : Scope.values()) {
      injector.addScope(scope);
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
    try {
      this.injectionContext = injector.create(filter);
      this.plugins = plugins;
    }
    catch (Exception e) {
      throw new UnsupportedOperationException("handle me gracefully", e);
    }
  }

  public String getName() {
    return descriptor.getName();
  }

  public ClassLoader getClassLoader() {
    return injectionContext.getClassLoader();
  }

  public InjectionContext<?, ?> getInjectionContext() {
    return injectionContext;
  }

  public <T> T resolveBean(Class<T> beanType) {
    return injectionContext.resolveInstance(beanType);
  }

  public <T> Iterable<T> resolveBeans(final Class<T> beanType) {
    return injectionContext.resolveInstances(beanType);
  }

  public ApplicationPlugin getPlugin(String pluginName) {
    return plugins.get(pluginName);
  }

  public ApplicationDescriptor getDescriptor() {
    return descriptor;
  }

  public Object resolveBean(String name) throws InvocationTargetException {
    return resolveBean(injectionContext, name);
  }

  private <B, I> Object resolveBean(InjectionContext<B, I> manager, String name) throws InvocationTargetException {
    B bean = manager.resolveBean(name);
    if (bean != null) {
      I cc = manager.createContext(bean);
      return manager.getInstance(bean, cc);
    }
    else {
      return null;
    }
  }
}
