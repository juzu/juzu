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

import juzu.Param;
import juzu.impl.common.MethodHandle;
import juzu.impl.common.ParameterMap;
import juzu.impl.common.Tools;
import juzu.request.Phase;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A controller method.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class MethodDescriptor {

  /** . */
  private final String id;

  /** . */
  private final Phase phase;

  /** . */
  private final Class<?> type;

  /** . */
  private final Method method;

  /** . */
  private final List<ParameterDescriptor> argumentList;

  /** . */
  private final Map<String, ParameterDescriptor> argumentMap;

  /** . */
  private final boolean requiresPrefix;

  /** . */
  private final MethodHandle handle;

  public MethodDescriptor(
      String id,
      Phase phase,
      Class<?> type,
      Method method,
      List<ParameterDescriptor> argumentList) {

    //
    LinkedHashMap<String, ParameterDescriptor> argumentMap = new LinkedHashMap<String, ParameterDescriptor>();
    for (ParameterDescriptor argument : argumentList) {
      argumentMap.put(argument.getName(), argument);
    }

    //
    boolean requiresPrefix = false;
    HashSet<String> set = new HashSet<String>();
    for (ParameterDescriptor argument : argumentList) {
      if (argument.getType() == String.class) {
        if (!set.add(argument.getName())) {
          requiresPrefix = true;
          break;
        }
      }
      else {
        for (Field field : argument.getType().getFields()) {
          if (!set.add(field.getName())) {
            requiresPrefix = true;
            break;
          }
        }
        for (Method beanMethod : argument.getType().getMethods()) {
          String methodName = beanMethod.getName();
          if (methodName.length() > 3 &&
            methodName.startsWith("get") &&
            beanMethod.getParameterTypes().length == 0 &&
            beanMethod.getReturnType() != Void.class) {
            String foo = methodName.substring(3);
            String name = Character.toLowerCase(methodName.charAt(0)) + foo.substring(1);
            if (!set.add(name)) {
              requiresPrefix = true;
              break;
            }
          }
        }
      }
    }

    //
    this.id = id;
    this.phase = phase;
    this.type = type;
    this.method = method;
    this.argumentList = Tools.safeUnmodifiableList(argumentList);
    this.argumentMap = Collections.unmodifiableMap(argumentMap);
    this.requiresPrefix = requiresPrefix;
    this.handle = new MethodHandle(method);
  }

  public MethodHandle getHandle() {
    return handle;
  }

  public String getId() {
    return id;
  }

  public Phase getPhase() {
    return phase;
  }

  public Class<?> getType() {
    return type;
  }

  public Method getMethod() {
    return method;
  }

  public String getName() {
    return method.getName();
  }

  public ParameterDescriptor getArgument(String name) {
    return argumentMap.get(name);
  }

  public List<ParameterDescriptor> getArguments() {
    return argumentList;
  }

  public Set<String> getArgumentNames() {
    return argumentMap.keySet();
  }

  public void setArgs(Object[] args, ParameterMap parameterMap) {
    for (int j = 0;j < argumentList.size();j++) {
      Object value = args[j];
      if (value != null) {
        ParameterDescriptor parameter = argumentList.get(j);
        String name = parameter.getName();
        switch (parameter.getCardinality()) {
          case SINGLE: {
            if (parameter.getType().isAnnotationPresent(Param.class)) {
              Map<String, String[]> p = buildBeanParameter(name, value);
              parameterMap.setParameters(p);
            }
            else {
              parameterMap.setParameter(name, String.valueOf(value));
            }
            break;
          }
          case ARRAY: {
            int length = Array.getLength(value);
            String[] array = new String[length];
            for (int i = 0;i < length;i++) {
              Object component = Array.get(value, i);
              array[i] = String.valueOf(component);
            }
            parameterMap.setParameter(name, array);
            break;
          }
          case LIST: {
            Collection<?> c = (Collection<?>)value;
            int length = c.size();
            String[] array = new String[length];
            Iterator<?> iterator = c.iterator();
            for (int i = 0;i < length;i++) {
              Object element = iterator.next();
              array[i] = String.valueOf(element);
            }
            parameterMap.setParameter(name, array);
            break;
          }
          default:
            throw new UnsupportedOperationException("Not yet implemented");
        }


        // Yeah OK nasty cast, we'll see later
        parameter.setValue(parameterMap, value);
      }
    }
  }

  private Map<String, String[]> buildBeanParameter(String baseName, Object value) {
    Map<String, String[]> parameters = new HashMap<String, String[]>();

    try {
      for (Field f : value.getClass().getFields()) {
        Object v = f.get(value);
        if (v == null) {
          continue;
        }

        String name = requiresPrefix ? baseName + "." + f.getName() : f.getName();
        addParameter(parameters, name, f.getType(), v);
      }

      for (Method m : value.getClass().getMethods()) {
        if (m.getName().startsWith("get") && m.getName().length() > 3 && m.getParameterTypes().length == 0) {
          Object v = m.invoke(value);
          if (v == null) {
            continue;
          }

          String n = Character.toLowerCase(m.getName().charAt(3)) + m.getName().substring(4);
          String name = requiresPrefix ? baseName + "." + n : n;
          addParameter(parameters, name, m.getReturnType(), v);
        }
      }
    }
    catch (Exception e) {
    }

    return parameters;
  }

  private void addParameter(Map<String, String[]> parameters, String name, Class clazz, Object value) {
    if (String.class.equals(clazz)) {
      parameters.put(name, new String[]{(String)value});
    }
    else if (String[].class.equals((clazz))) {
      parameters.put(name, (String[])value);
    }
    else if (List.class.equals((clazz))) {
      parameters.put(name, (String[])((List)value).toArray());
    }
  }

  public Object[] getArgs(Map<String, String[]> parameterMap) {
    // Prepare method parameters
    Class<?>[] paramsType = method.getParameterTypes();
    Object[] args = new Object[argumentList.size()];
    for (int i = 0;i < args.length;i++) {
      ParameterDescriptor parameter = argumentList.get(i);
      Object[] values;
      if (paramsType[i].isAnnotationPresent(Param.class)) {
        // build bean parameter
        Object o = null;
        try {
          o = createMappedBean(paramsType[i], parameter.getName(), parameterMap);
        }
        catch (Exception e) {
        }
        values = new Object[]{o};
      }
      else {
        values = parameterMap.get(parameter.getName());
      }
      if (values != null) {
        switch (parameter.getCardinality()) {
          case SINGLE:
            args[i] = (values.length > 0) ? values[0] : null;
            break;
          case ARRAY:
            args[i] = values.clone();
            break;
          case LIST:
            ArrayList<Object> list = new ArrayList<Object>(values.length);
            Collections.addAll(list, values);
            args[i] = list;
            break;
          default:
            throw new UnsupportedOperationException("Handle me gracefully");
        }
      }
    }

    //
    return args;
  }

  private <T> T createMappedBean(Class<T> clazz, String beanName, Map<String, String[]> parameters) throws IllegalAccessException, InstantiationException {
    // Extract parameters
    Map<String, String[]> beanParams = new HashMap<String, String[]>();
    String prefix = requiresPrefix ? beanName + "." : "";
    for (String key : parameters.keySet()) {
      if (key.startsWith(prefix)) {
        String paramName = key.substring(prefix.length());
        beanParams.put(paramName, parameters.get(key));
      }
    }

    // Build bean
    T bean = clazz.newInstance();
    for (String key : beanParams.keySet()) {
      String[] value = beanParams.get(key);
      String setterName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
      boolean success = callSetter(setterName, clazz, bean, value, String[].class);
      if (!success) {
        success = callSetter(setterName, clazz, bean, value[0], String.class);
      }
      if (!success) {
        success = callSetter(setterName, clazz, bean, Arrays.asList(value), List.class);
      }
      if (!success) {
        try {
          Field f = clazz.getField(key);
          if (String[].class.equals(f.getType())) {
            f.set(bean, value);
          }
          else if (String.class.equals(f.getType())) {
            f.set(bean, value[0]);
          }
          else if (List.class.equals(f.getType())) {
            f.set(bean, Arrays.asList(value));
          }

        }
        catch (NoSuchFieldException e) {
        }
      }

    }
    return bean;
  }

  <T> boolean callSetter(String methodName, Class<T> clazz, T target, Object value, Class type) {
    try {
      Method m = clazz.getMethod(methodName, type);
      m.invoke(target, value);
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getSimpleName());
    sb.append("[type=").append(type.getName()).append(",method=");
    sb.append(method.getName()).append("(");
    Class<?>[] types = method.getParameterTypes();
    for (int i = 0;i < types.length;i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(argumentList.get(i).getName()).append("=").append(types[i].getName());
    }
    sb.append(")]");
    return sb.toString();
  }
}
