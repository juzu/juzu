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
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationDescriptor extends Descriptor {

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
  private final Map<String, Descriptor> plugins;

  /** . */
  private final Map<String, ApplicationPlugin> foo;

  public ApplicationDescriptor(Class<?> applicationClass) {
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

    //
    HashMap<String, Descriptor> pluginDescriptors = new HashMap<String, Descriptor>();
    for (String name : config.names()) {
      Plugin plugin = pluginMap.get(name);
      if (plugin == null) {
        throw new UnsupportedOperationException("Handle me gracefully : missing plugin " + name);
      }
      JSON pluginConfig = config.getJSON(name);
      try {
        Descriptor pluginDescriptor = plugin.init(applicationClass.getClassLoader(), pluginConfig);
        pluginDescriptors.put(name, pluginDescriptor);
      }
      catch (Exception e) {
        AssertionError ae = new AssertionError("Cannot load config");
        ae.initCause(e);
        throw ae;
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
    this.applicationClass = applicationClass;
    this.name = applicationClass.getSimpleName();
    this.packageName = applicationClass.getPackage().getName();
    this.templates = (TemplatesDescriptor)pluginDescriptors.get("plugin/template");
    this.packageClass = packageClass;
    this.controllers = (ControllersDescriptor)pluginDescriptors.get("controller");
    this.plugins = pluginDescriptors;
    this.foo = pluginMap;
  }

  public Map<String, ApplicationPlugin> getFoo() {
    return foo;
  }

  @Override
  public Iterable<BeanDescriptor> getBeans() {
    ArrayList<BeanDescriptor> beans = new ArrayList<BeanDescriptor>();
    for (Descriptor descriptor : plugins.values()) {
      Tools.addAll(beans, descriptor.getBeans());
    }
    return beans;
  }

  public Descriptor getPlugin(String name) {
    return plugins.get(name);
  }

  public void addPlugin(String name, Descriptor plugin) {
    plugins.put(name, plugin);
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
