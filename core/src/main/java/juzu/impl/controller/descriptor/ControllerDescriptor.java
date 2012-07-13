package juzu.impl.controller.descriptor;

import juzu.impl.inject.BeanDescriptor;
import juzu.impl.metadata.Descriptor;
import juzu.impl.common.JSON;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerDescriptor extends Descriptor {

  /** . */
  private final Class<?> defaultController;

  /** . */
  private final List<ControllerBean> controllers;

  /** . */
  private final List<ControllerMethod> methods;

  /** . */
  private final ArrayList<BeanDescriptor> beans;

  /** . */
  private final Boolean escapeXML;

  /** . */
  private final List<ControllerRoute> routes;

  public ControllerDescriptor(ClassLoader loader, JSON config) throws Exception {
    List<ControllerBean> controllers = new ArrayList<ControllerBean>();
    List<ControllerMethod> controllerMethods = new ArrayList<ControllerMethod>();
    ArrayList<BeanDescriptor> beans = new ArrayList<BeanDescriptor>();

    // Load controllers
    for (String fqn : config.getList("controllers", String.class)) {
      Class<?> clazz = loader.loadClass(fqn);
      Field f = clazz.getField("DESCRIPTOR");
      ControllerBean controller = (ControllerBean)f.get(null);
      controllers.add(controller);
      controllerMethods.addAll(controller.getMethods());
      beans.add(new BeanDescriptor(controller.getType(), null, null, null));
    }

    //
    Boolean escapeXML = config.getBoolean("escapeXML");

    //
    Class<?> defaultController = null;
    String defaultControllerName = config.getString("default");
    if (defaultControllerName != null) {
      defaultController = loader.loadClass(defaultControllerName);
    }

    // Routes
    List<ControllerRoute> routes = new ArrayList<ControllerRoute>();
    List<? extends JSON> abc = config.getList("routes", JSON.class);
    if (abc != null) {
      for (JSON route : abc) {
        String id = route.getString("id");
        List<? extends String> parameters = route.getList("parameters", String.class);
        String path = route.getString("path");
        ControllerRoute r = new ControllerRoute(
            id,
            Collections.unmodifiableSet(new HashSet<String>(parameters)),
            path
        );
        routes.add(r);
      }
    }

    //
    this.escapeXML = escapeXML;
    this.defaultController = defaultController;
    this.controllers = controllers;
    this.methods = controllerMethods;
    this.beans = beans;
    this.routes = Collections.unmodifiableList(routes);
  }

  public Iterable<BeanDescriptor> getBeans() {
    return beans;
  }

  public Class<?> getDefault() {
    return defaultController;
  }

  public Boolean getEscapeXML() {
    return escapeXML;
  }

  public List<ControllerBean> getControllers() {
    return controllers;
  }

  public List<ControllerMethod> getMethods() {
    return methods;
  }

  public List<ControllerRoute> getRoutes() {
    return routes;
  }

  public ControllerMethod getMethod(Class<?> type, String name, Class<?>... parameterTypes) {
    for (int i = 0;i < methods.size();i++) {
      ControllerMethod cm = methods.get(i);
      Method m = cm.getMethod();
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

  public ControllerMethod getMethodById(String methodId) {
    for (int i = 0;i < methods.size();i++) {
      ControllerMethod cm = methods.get(i);
      if (cm.getId().equals(methodId)) {
        return cm;
      }
    }
    return null;
  }
}
