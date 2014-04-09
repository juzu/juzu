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

package juzu.impl.plugin.controller.descriptor;

import juzu.Response;
import juzu.impl.common.MethodHandle;
import juzu.impl.plugin.PluginDescriptor;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.plugin.controller.ControllerResolver;
import juzu.impl.common.JSON;
import juzu.impl.request.ControllerHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllersDescriptor extends PluginDescriptor {

  /** . */
  private final Class<?> defaultController;

  /** . */
  private final Class<? extends juzu.Handler<Response.Error, Response>> errorController;

  /** . */
  private final List<ControllerDescriptor> controllers;

  /** . */
  private final ArrayList<ControllerHandler> handlers;

  /** . */
  private final ArrayList<juzu.impl.inject.BeanDescriptor> beans;

  /** . */
  private final Boolean escapeXML;

  /** . */
  private ControllerDescriptorResolver resolver;

  /** . */
  private final Map<MethodHandle, ControllerHandler> byHandle;

  public ControllersDescriptor(ApplicationDescriptor desc) throws Exception {
    this(desc.getApplicationLoader(), desc.getConfig().getJSON("controller"));
  }

  public ControllersDescriptor(ClassLoader loader, JSON config) throws Exception {
    List<ControllerDescriptor> controllers = new ArrayList<ControllerDescriptor>();
    ArrayList<ControllerHandler> handlers = new ArrayList<ControllerHandler>();
    ArrayList<juzu.impl.inject.BeanDescriptor> beans = new ArrayList<juzu.impl.inject.BeanDescriptor>();
    HashMap<MethodHandle, ControllerHandler> byHandle = new HashMap<MethodHandle, ControllerHandler>();

    // Load controllers
    for (String fqn : config.getList("controllers", String.class)) {
      Class<?> clazz = loader.loadClass(fqn);
      Field f = clazz.getField("DESCRIPTOR");
      ControllerDescriptor bean = (ControllerDescriptor)f.get(null);
      for (ControllerHandler handler : bean.getHandlers()) {
        byHandle.put(handler.getHandle(), handler);
      }
      controllers.add(bean);
      handlers.addAll(bean.getHandlers());
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
    Class<? extends juzu.Handler<Response.Error, Response>> errorController = null;
    String errorControllerName = config.getString("error");
    if (errorControllerName != null) {
      errorController = (Class<? extends juzu.Handler<Response.Error,Response>>)loader.loadClass(errorControllerName);
      beans.add(BeanDescriptor.createFromBean(errorController, null, Collections.<Annotation>emptyList()));
    }

    //
    this.escapeXML = escapeXML;
    this.defaultController = defaultController;
    this.errorController = errorController;
    this.controllers = controllers;
    this.handlers = handlers;
    this.beans = beans;
    this.resolver = new ControllerDescriptorResolver(this);
    this.byHandle = byHandle;
  }

  public Iterable<juzu.impl.inject.BeanDescriptor> getBeans() {
    return beans;
  }

  public ControllerResolver<ControllerHandler> getResolver() {
    return resolver;
  }

  public Class<?> getDefaultController() {
    return defaultController;
  }

  public Class<? extends juzu.Handler<Response.Error, Response>> getErrorController() {
    return errorController;
  }

  public Boolean getEscapeXML() {
    return escapeXML;
  }

  public List<ControllerDescriptor> getControllers() {
    return controllers;
  }

  public List<ControllerHandler> getHandlers() {
    return handlers;
  }

  public ControllerHandler getMethod(Class<?> type, String name, Class<?>... parameterTypes) {
    for (int i = 0;i < handlers.size();i++) {
      ControllerHandler cm = handlers.get(i);
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

  public ControllerHandler getMethodById(String methodId) {
    for (int i = 0;i < handlers.size();i++) {
      ControllerHandler cm = handlers.get(i);
      if (cm.getId().equals(methodId)) {
        return cm;
      }
    }
    return null;
  }

  public ControllerHandler getMethodByHandle(MethodHandle handle) {
    return byHandle.get(handle);
  }
}
