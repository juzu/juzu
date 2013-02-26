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

package juzu.impl.plugin.controller.descriptor;

import juzu.impl.common.MethodHandle;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.plugin.controller.ControllerResolver;
import juzu.impl.metadata.Descriptor;
import juzu.impl.common.JSON;
import juzu.impl.request.Method;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllersDescriptor extends Descriptor {

  /** . */
  private final Class<?> defaultController;

  /** . */
  private final List<ControllerDescriptor> controllers;

  /** . */
  private final ArrayList<Method> methods;

  /** . */
  private final ArrayList<juzu.impl.inject.BeanDescriptor> beans;

  /** . */
  private final Boolean escapeXML;

  /** . */
  private ControllerDescriptorResolver resolver;

  /** . */
  private final Map<MethodHandle, Method> byHandle;

  public ControllersDescriptor(ApplicationDescriptor desc) throws Exception {
    this(desc.getApplicationLoader(), desc.getConfig().getJSON("controller"));
  }

  public ControllersDescriptor(ClassLoader loader, JSON config) throws Exception {
    List<ControllerDescriptor> controllers = new ArrayList<ControllerDescriptor>();
    ArrayList<Method> controllerMethods = new ArrayList<Method>();
    ArrayList<juzu.impl.inject.BeanDescriptor> beans = new ArrayList<juzu.impl.inject.BeanDescriptor>();
    HashMap<MethodHandle, Method> byHandle = new HashMap<MethodHandle, Method>();

    // Load controllers
    for (String fqn : config.getList("controllers", String.class)) {
      Class<?> clazz = loader.loadClass(fqn);
      Field f = clazz.getField("DESCRIPTOR");
      ControllerDescriptor bean = (ControllerDescriptor)f.get(null);
      for (Method method : bean.getMethods()) {
        byHandle.put(method.getHandle(), method);
      }
      controllers.add(bean);
      controllerMethods.addAll(bean.getMethods());
      beans.add(BeanDescriptor.createFromBean(bean.getType(), null, null));
    }

    //
    Boolean escapeXML = config.getBoolean("escapeXML");

    //
    Class<?> defaultController = null;
    String defaultControllerName = config.getString("default");
    if (defaultControllerName != null) {
      defaultController = loader.loadClass(defaultControllerName);
    }

    //
    this.escapeXML = escapeXML;
    this.defaultController = defaultController;
    this.controllers = controllers;
    this.methods = controllerMethods;
    this.beans = beans;
    this.resolver = new ControllerDescriptorResolver(this);
    this.byHandle = byHandle;
  }

  public Iterable<juzu.impl.inject.BeanDescriptor> getBeans() {
    return beans;
  }

  public ControllerResolver<Method> getResolver() {
    return resolver;
  }

  public Class<?> getDefault() {
    return defaultController;
  }

  public Boolean getEscapeXML() {
    return escapeXML;
  }

  public List<ControllerDescriptor> getControllers() {
    return controllers;
  }

  public List<Method> getMethods() {
    return methods;
  }

  public Method getMethod(Class<?> type, String name, Class<?>... parameterTypes) {
    for (int i = 0;i < methods.size();i++) {
      Method cm = methods.get(i);
      java.lang.reflect.Method m = cm.getMethod();
      if (type.equals(cm.getType()) && m.getName().equals(name)) {
        Class<?>[] a = m.getParameterTypes();
        if (a.length == parameterTypes.length) {
          for (int j = 0;j < parameterTypes.length;j++) {
            if (!a[j].equals(parameterTypes[j])) {
              continue;
            }
          }
          return cm;
        }
      }
    }
    return null;
  }

  public Method getMethodById(String methodId) {
    for (int i = 0;i < methods.size();i++) {
      Method cm = methods.get(i);
      if (cm.getId().equals(methodId)) {
        return cm;
      }
    }
    return null;
  }

  public Method getMethodByHandle(MethodHandle handle) {
    return byHandle.get(handle);
  }
}
