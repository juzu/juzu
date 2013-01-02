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

package juzu.impl.plugin.application.descriptor;

import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.plugin.controller.descriptor.ControllersDescriptor;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.Plugin;
import juzu.impl.plugin.template.metadata.TemplatesDescriptor;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationDescriptor extends Descriptor {

  /**
   * Encapsulate application descriptor loading from an application class.
   *
   * @param applicationClass the application class
   * @return the descriptor
   * @throws Exception any exception that would prevent the exception to be loaded
   */
  public static ApplicationDescriptor create(Class<?> applicationClass) throws Exception {
    return new ApplicationDescriptor(applicationClass);
  }

  /** . */
  private final Class<?> applicationClass;

  /** . */
  private final String packageName;

  /** . */
  private final String name;

  /** . */
  private final Class<?> packageClass;

  /** . */
  private final ControllersDescriptor controllers;

  /** . */
  private final TemplatesDescriptor templates;

  /** . */
  private final Map<String, Descriptor> pluginDescriptors;

  /** . */
  private final Map<String, ApplicationPlugin> plugins;

  public ApplicationDescriptor(Class<?> applicationClass) throws Exception {
    // Load config
    JSON config;
    InputStream in = null;
    try {
      in = applicationClass.getResourceAsStream("config.json");
      String s = Tools.read(in);
      config = (JSON)JSON.parse(s);
    }
    catch (IOException e) {
      throw new AssertionError(e);
    }
    finally {
      Tools.safeClose(in);
    }

    //
    Class<?> packageClass;
    try {
      packageClass = applicationClass.getClassLoader().loadClass(applicationClass.getPackage().getName() + ".package-info");
    }
    catch (ClassNotFoundException e) {
      AssertionError ae = new AssertionError("Cannot load package class");
      ae.initCause(e);
      throw ae;
    }

    //
    HashMap<String, ApplicationPlugin> pluginMap = new HashMap<String, ApplicationPlugin>();
    for (ApplicationPlugin plugin : ServiceLoader.load(ApplicationPlugin.class)) {
      pluginMap.put(plugin.getName(), plugin);
    }

    // Init this first before initing plugin so they can use it
    this.applicationClass = applicationClass;
    this.name = applicationClass.getSimpleName();
    this.packageName = applicationClass.getPackage().getName();
    this.packageClass = packageClass;

    //
    HashSet<String> names = new HashSet<String>(config.names());
    HashMap<ApplicationPlugin, JSON> configs = new HashMap<ApplicationPlugin, JSON>();
    for (ApplicationPlugin plugin : pluginMap.values()) {
      String name = plugin.getName();
      if (names.remove(name)) {
        configs.put(plugin, config.getJSON(plugin.getName()));
      } else {
        configs.put(plugin, null);
      }
    }

    //
    if (names.size() > 0) {
      throw new UnsupportedOperationException("Handle me gracefully : missing plugins " + names);
    }

    //
    HashMap<String, Descriptor> pluginDescriptors = new HashMap<String, Descriptor>();
    for (Map.Entry<ApplicationPlugin, JSON> entry : configs.entrySet()) {
      ApplicationPlugin plugin = entry.getKey();
      Descriptor descriptor = plugin.init(this, entry.getValue());
      if (descriptor != null) {
        pluginDescriptors.put(plugin.getName(), descriptor);
      }
    }

    //
    for (Iterator<String> i = pluginMap.keySet().iterator();i.hasNext();) {
      String name = i.next();
      if (!pluginDescriptors.containsKey(name)) {
        i.remove();
      }
    }

    //
    this.templates = (TemplatesDescriptor)pluginDescriptors.get("template");
    this.controllers = (ControllersDescriptor)pluginDescriptors.get("controller");
    this.pluginDescriptors = pluginDescriptors;
    this.plugins = pluginMap;
  }

  public Map<String, ApplicationPlugin> getPlugins() {
    return plugins;
  }

  @Override
  public Iterable<BeanDescriptor> getBeans() {
    ArrayList<BeanDescriptor> beans = new ArrayList<BeanDescriptor>();
    for (Descriptor descriptor : pluginDescriptors.values()) {
      Tools.addAll(beans, descriptor.getBeans());
    }
    return beans;
  }

  public Descriptor getPluginDescriptor(String name) {
    return pluginDescriptors.get(name);
  }

  public void addPlugin(ApplicationPlugin plugin) throws Exception {
    plugins.put(plugin.getName(), plugin);
    pluginDescriptors.put(name,  plugin.init(this, new JSON()));
  }

  public Class<?> getPackageClass() {
    return packageClass;
  }

  public Class<?> getApplicationClass() {
    return applicationClass;
  }

  public ClassLoader getApplicationLoader() {
    return applicationClass.getClassLoader();
  }

  public String getPackageName() {
    return packageName;
  }

  public String getName() {
    return name;
  }

  public ControllersDescriptor getControllers() {
    return controllers;
  }

  public TemplatesDescriptor getTemplates() {
    return templates;
  }
}
