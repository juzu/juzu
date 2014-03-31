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

import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.value.ValueType;
import juzu.request.RequestParameter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A bean control parameter.
 *
 * @author Julien Viet
 */
public class BeanParameter extends ControlParameter {

  public BeanParameter(String name, Class<?> type) throws NullPointerException {
    super(name, type);
  }

  <T> T createMappedBean(ControllerPlugin plugin, boolean requiresPrefix, Class<T> clazz, String beanName, Map<String, RequestParameter> parameters) throws IllegalAccessException, InstantiationException {
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
      try {
        boolean success = callSetter(plugin, setterName, clazz, bean, value);
        if (!success) {
          Field f = clazz.getField(key);
          Object o = getValue(plugin, f.getGenericType(), value);
          if (o != null) {
            f.set(bean, o);
          }
        }
      }
      catch (Exception e) {
        // Do something better
      }

    }
    return bean;
  }

  Object getValue(ControllerPlugin plugin, Type type, String[] value) throws Exception {
    if (type instanceof Class<?>) {
      Class<?> clazz = (Class<?>)type;
      if (clazz.isArray()) {
        clazz = clazz.getComponentType();
        ValueType valueType = plugin.resolveValueType(clazz);
        if (valueType != null) {
          Object array = Array.newInstance(clazz, value.length);
          for (int i = 0;i < value.length;i++) {
            Array.set(array, i, valueType.parse(value[i]));
          }
          return array;
        }
      } else {
        ValueType valueType = plugin.resolveValueType(clazz);
        if (valueType != null) {
          return valueType.parse(value[0]);
        }
      }
    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType)type;
      if (List.class.equals(parameterizedType.getRawType())) {
        Type typeArg = parameterizedType.getActualTypeArguments()[0];
        if (typeArg instanceof Class) {
          ValueType valueType = plugin.resolveValueType((Class)typeArg);
          if (valueType != null) {
            ArrayList list = new ArrayList(value.length);
            for (String s : value) {
              list.add(valueType.parse(s));
            }
            return list;
          }
        }
      }
    }
    return null;
  }

  <T> boolean callSetter(ControllerPlugin plugin, String methodName, Class<T> clazz, T target, String[] value) throws Exception {
    for (java.lang.reflect.Method m : clazz.getMethods()) {
      if (m.getName().equals(methodName)) {
        int modifiers = m.getModifiers();
        if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
          Type[] parameterTypes = m.getGenericParameterTypes();
          if (parameterTypes.length == 1) {
            Object o = getValue(plugin, parameterTypes[0], value);
            if (o != null) {
              m.invoke(target, o);
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  Map<String, String[]> buildBeanParameter(ControllerPlugin plugin, boolean requiresPrefix, String baseName, Object value) {
    Map<String, String[]> parameters = new HashMap<String, String[]>();

    try {
      for (Field f : value.getClass().getFields()) {
        if (Modifier.isPublic(f.getModifiers())) {
          Object v = f.get(value);
          if (v != null) {
            String name = requiresPrefix ? baseName + "." + f.getName() : f.getName();
            addParameter(plugin, parameters, name, f.getGenericType(), v);
          }
        }
      }

      for (java.lang.reflect.Method m : value.getClass().getMethods()) {
        if (Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers()) && m.getName().startsWith("get") && m.getName().length() > 3 && m.getParameterTypes().length == 0) {
          Object v = m.invoke(value);
          if (v != null) {
            String n = Character.toLowerCase(m.getName().charAt(3)) + m.getName().substring(4);
            String name = requiresPrefix ? baseName + "." + n : n;
            addParameter(plugin, parameters, name, m.getGenericReturnType(), v);
          }
        }
      }
    }
    catch (Exception e) {
    }

    return parameters;
  }

  private void addParameter(ControllerPlugin plugin, Map<String, String[]> parameters, String name, Type type, Object value) {
    String[] v = getParameters(plugin, type, value);
    if (v != null) {
      parameters.put(name, v);
    }
  }

  private String[] getParameters(ControllerPlugin plugin, Type type, Object value) {
    if (type instanceof Class) {
      Class clazz = (Class)type;
      if (clazz.isArray()) {
        clazz = clazz.getComponentType();
        int length = Array.getLength(value);
        if (length > 0) {
          ValueType valueType = plugin.resolveValueType(clazz);
          if (valueType != null) {
            String[] ret = new String[length];
            for (int i = 0;i < length;i++) {
              Object element = Array.get(value, i);
              ret[i] = valueType.format(element);
            }
            return ret;
          }
        }
      } else {
        ValueType valueType = plugin.resolveValueType(clazz);
        if (valueType != null) {
          return new String[]{valueType.format(value)};
        }
      }
    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType)type;
      if (parameterizedType.getRawType().equals(List.class)) {
        List list = (List)value;
        int size = list.size();
        if (size > 0) {
          Type typeArg = parameterizedType.getActualTypeArguments()[0];
          if (typeArg instanceof Class) {
            ValueType valueType = plugin.resolveValueType((Class)typeArg);
            if (valueType != null) {
              String[] ret = new String[size];
              for (int i = 0;i < size;i++) {
                Object element = list.get(i);
                ret[i] = valueType.format(element);
              }
              return ret;
            }
          }
        }
      }
    }
    return null;
  }
}
