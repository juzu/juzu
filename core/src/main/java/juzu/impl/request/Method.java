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

package juzu.impl.request;

import juzu.Mapped;
import juzu.impl.bridge.Parameters;
import juzu.impl.common.MethodHandle;
import juzu.impl.common.Tools;
import juzu.request.Phase;
import juzu.request.RequestParameter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
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
public final class Method<P extends Phase> {

  /** . */
  private final String id;

  /** . */
  private final P phase;

  /** . */
  private final Class<?> type;

  /** . */
  private final java.lang.reflect.Method method;

  /** . */
  private final List<ControlParameter> parameterList;

  /** . */
  private final Map<String, ControlParameter> parameterMap;

  /** . */
  private final boolean requiresPrefix;

  /** . */
  private final MethodHandle handle;

  public Method(
      String id,
      P phase,
      Class<?> type,
      java.lang.reflect.Method method,
      List<ControlParameter> parameterList) {

    // Fix parameter list
    Class<?>[] parameterTypes = method.getParameterTypes();
    Type[] genericParameterTypes = method.getGenericParameterTypes();
    parameterList = new ArrayList<ControlParameter>(parameterList);
    for (int i = 0;i < parameterList.size();i++) {
      ControlParameter parameter = parameterList.get(i);
      if (parameter instanceof ContextualParameter) {
        Type genericParameterType = genericParameterTypes[i];
        parameterList.set(i, new ContextualParameter(parameter.getName(), parameterTypes[i], genericParameterType));
      }
    }

    //
    LinkedHashMap<String, ControlParameter> argumentMap = new LinkedHashMap<String, ControlParameter>();
    for (ControlParameter argument : parameterList) {
      argumentMap.put(argument.getName(), argument);
    }

    //
    boolean requiresPrefix = false;
    HashSet<String> set = new HashSet<String>();
    for (ControlParameter parameter : parameterList) {
      if (parameter instanceof PhaseParameter) {
        PhaseParameter phaseParameter = (PhaseParameter)parameter;
        if (phaseParameter.getType() == String.class) {
          if (!set.add(parameter.getName())) {
            requiresPrefix = true;
            break;
          }
        }
        else {
          for (Field field : phaseParameter.getType().getFields()) {
            if (!set.add(field.getName())) {
              requiresPrefix = true;
              break;
            }
          }
          for (java.lang.reflect.Method beanMethod : phaseParameter.getType().getMethods()) {
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
    }

    //
    this.id = id;
    this.phase = phase;
    this.type = type;
    this.method = method;
    this.parameterList = Tools.safeUnmodifiableList(parameterList);
    this.parameterMap = Collections.unmodifiableMap(argumentMap);
    this.requiresPrefix = requiresPrefix;
    this.handle = new MethodHandle(method);
  }

  public MethodHandle getHandle() {
    return handle;
  }

  public String getId() {
    return id;
  }

  public P getPhase() {
    return phase;
  }

  public Class<?> getType() {
    return type;
  }

  public java.lang.reflect.Method getMethod() {
    return method;
  }

  public String getName() {
    return method.getName();
  }

  public ControlParameter getParameter(String name) {
    return parameterMap.get(name);
  }

  public List<ControlParameter> getParameters() {
    return parameterList;
  }

  public Set<String> getParameterNames() {
    return parameterMap.keySet();
  }

  public void setArgs(Object[] args, Parameters parameterMap) {
    int index = 0;
    for (ControlParameter parameter : parameterList) {
      if (parameter instanceof PhaseParameter) {
        PhaseParameter phaseParameter = (PhaseParameter)parameter;
        Object value = args[index++];
        if (value != null) {
          String name = phaseParameter.getMappedName();
          switch (phaseParameter.getCardinality()) {
            case SINGLE: {
              if (phaseParameter.getType().isAnnotationPresent(Mapped.class)) {
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
        }
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

      for (java.lang.reflect.Method m : value.getClass().getMethods()) {
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

  public Map<ControlParameter, Object> getArguments(Map<String, RequestParameter> parameterMap) {
    Map<ControlParameter, Object> arguments = new HashMap<ControlParameter, Object>();
    for (ControlParameter controlParam : this.parameterMap.values()) {
      if (controlParam instanceof PhaseParameter) {
        PhaseParameter phaseParameter = (PhaseParameter)controlParam;
        Class<?> type = phaseParameter.getType();
        Object[] values;
        if (type.isAnnotationPresent(Mapped.class)) {
          // build bean parameter
          Object o = null;
          try {
            o = createMappedBean(type, phaseParameter.getMappedName(), parameterMap);
          }
          catch (Exception e) {
          }
          values = new Object[]{o};
        }
        else {
          RequestParameter requestParam = parameterMap.get(phaseParameter.getMappedName());
          values = requestParam != null ? requestParam.toArray() : null;
        }
        if (values != null) {
          Object arg;
          switch (phaseParameter.getCardinality()) {
            case SINGLE:
              arg = (values.length > 0) ? values[0] : null;
              break;
            case ARRAY:
              arg = values.clone();
              break;
            case LIST:
              ArrayList<Object> list = new ArrayList<Object>(values.length);
              Collections.addAll(list, values);
              arg = list;
              break;
            default:
              throw new UnsupportedOperationException("Handle me gracefully");
          }
          arguments.put(controlParam, arg);
        }
      }
    }
    return arguments;
  }

  private <T> T createMappedBean(Class<T> clazz, String beanName, Map<String, RequestParameter> parameters) throws IllegalAccessException, InstantiationException {
    // Extract parameters
    Map<String, String[]> beanParams = new HashMap<String, String[]>();
    String prefix = requiresPrefix ? beanName + "." : "";
    for (String key : parameters.keySet()) {
      if (key.startsWith(prefix)) {
        String paramName = key.substring(prefix.length());
        beanParams.put(paramName, parameters.get(key).toArray());
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
      java.lang.reflect.Method m = clazz.getMethod(methodName, type);
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
      sb.append(parameterList.get(i).getName()).append("=").append(types[i].getName());
    }
    sb.append(")]");
    return sb.toString();
  }
}
