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

package juzu.impl.plugin.module;

import juzu.impl.common.JSON;
import juzu.impl.plugin.PluginDescriptor;
import juzu.impl.plugin.PluginContext;
import juzu.impl.resource.ResourceResolver;

import java.net.URL;
import java.util.HashMap;
import java.util.ServiceLoader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Module {

  /** . */
  public final ModuleContext context;

  /** . */
  private final HashMap<String, ModulePlugin> plugins;

  /** . */
  private final HashMap<String, PluginDescriptor> descriptors;

  public Module(final ModuleContext context) throws Exception {

    //
    HashMap<String, ModulePlugin> plugins = new HashMap<String, ModulePlugin>();
    for (ModulePlugin plugin : ServiceLoader.load(ModulePlugin.class)) {
      plugins.put(plugin.getName(), plugin);
    }

    //
    final ResourceResolver classPathResolver = new ResourceResolver() {
      public URL resolve(String uri) {
        return context.getClassLoader().getResource(uri.substring(1));
      }
    };

    // Init plugins
    HashMap<String, PluginDescriptor> descriptors = new HashMap<String, PluginDescriptor>();
    for (ModulePlugin plugin : plugins.values()) {
      final JSON pluginConfig = context.getConfig().getJSON(plugin.getName());

      //
      PluginContext pluginContext = new PluginContext() {
        public JSON getConfig() {
          return pluginConfig;
        }
        public ClassLoader getClassLoader() {
          return context.getClassLoader();
        }
        public ResourceResolver getServerResolver() {
          return context.getServerResolver();
        }
        public ResourceResolver getApplicationResolver() {
          return classPathResolver;
        }
      };

      //
      PluginDescriptor desc = plugin.init(pluginContext);
      if (desc != null) {
        descriptors.put(plugin.getName(), desc);
      }
    }

    //
    this.plugins = plugins;
    this.descriptors = descriptors;
    this.context = context;
  }

  public ModulePlugin getPlugin(String name) {
    return plugins.get(name);
  }

  public <P extends ModulePlugin> P getPlugin(Class<P> type) {
    for (ModulePlugin plugin : plugins.values()) {
      if (type.isInstance(plugin)) {
        return type.cast(plugin);
      }
    }
    return null;
  }
}
