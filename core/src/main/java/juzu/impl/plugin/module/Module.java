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

import juzu.impl.common.JSON;
import juzu.impl.metadata.Descriptor;

import java.util.HashMap;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Module {

  /** . */
  final AtomicInteger leases;

  /** . */
  public final ModuleContext context;

  /** . */
  private final HashMap<String, ModulePlugin> plugins;

  /** . */
  private final HashMap<String, Descriptor> descriptors;

  public Module(ModuleContext context) throws Exception {

    //
    HashMap<String, ModulePlugin> plugins = new HashMap<String, ModulePlugin>();
    for (ModulePlugin plugin : ServiceLoader.load(ModulePlugin.class)) {
      plugins.put(plugin.getName(), plugin);
    }

    // Init plugins
    HashMap<String, Descriptor> descriptors = new HashMap<String, Descriptor>();
    for (ModulePlugin plugin : plugins.values()) {
      JSON pluginConfig = context.getConfig().getJSON(plugin.getName());
      Descriptor desc = plugin.init(context.getClassLoader(), pluginConfig);
      if (desc != null) {
        descriptors.put(plugin.getName(), desc);
      }
    }

    //
    this.plugins = plugins;
    this.descriptors = descriptors;
    this.context = context;
    this.leases = new AtomicInteger();
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

  public synchronized void lease() {
    leases.incrementAndGet();
  }


  /**
   * Return true if the module has no references pointing to it.
   *
   * @return true when the module is not referenced anymore
   */
  public synchronized boolean release() {
    return leases.decrementAndGet() == 0;
  }
}
