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

package juzu.impl.common;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Uniquely identifies a method.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class MethodHandle implements Iterable<String> {

  public static MethodHandle parse(String s) throws NullPointerException, IllegalArgumentException {
    int method = s.indexOf('#');
    if (method == -1) {
      throw new IllegalArgumentException("Invalid method handle " + s);
    }
    int leftParenthesis = s.indexOf('(', method + 1);
    if (leftParenthesis == -1 || leftParenthesis > s.length() - 2) {
      throw new IllegalArgumentException("Invalid method handle " + s);
    }
    if (s.charAt(s.length() - 1) != ')') {
      throw new IllegalArgumentException("Invalid method handle " + s);
    }

    //
    String type = s.substring(0, method);
    String name = s.substring(method + 1, leftParenthesis);

    //
    if (s.length() - leftParenthesis == 2) {
      return new MethodHandle(type, name);
    } else {
      String[] list = EMPTY_STRINGS;
      for (String parameter : Spliterator.split(s, leftParenthesis + 1, s.length() - 1, ',', new ArrayList<String>())) {
        if (parameter.length() == 0) {
          throw new IllegalArgumentException();
        }
        list = Tools.appendTo(list, parameter);
      }
      return new MethodHandle(type, name, list);
    }
  }

  /** . */
  private static final String[] EMPTY_STRINGS = new String[0];

  /** . */
  private final String type;

  /** . */
  private final String name;

  /** . */
  private final String[] parameters;

  /** . */
  private String toString;

  public MethodHandle(Method method) throws NullPointerException {
    if (method == null) {
      throw new NullPointerException("No null method accepted");
    }

    Type[] parameterTypes = method.getGenericParameterTypes();
    String[] parameters = new String[parameterTypes.length];
    StringBuilder sb = new StringBuilder();
    for (int i = 0;i < parameters.length;i++) {
      format(parameterTypes[i], sb);
      parameters[i] = sb.toString();
      sb.setLength(0);
    }

    //
    this.type = method.getDeclaringClass().getName();
    this.name = method.getName();
    this.parameters = parameters.clone();
    this.toString = null;
  }

  private void format(Type type, StringBuilder sb) {
    if (type instanceof Class) {
      Class classType = (Class)type;
      if (classType.isArray()) {
        format(classType.getComponentType(), sb);
        sb.append("[]");
      } else {
        sb.append(classType.getName());
      }
    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType)type;
      format(parameterizedType.getRawType(), sb);
      sb.append('<');
      Type[] typeArguments = parameterizedType.getActualTypeArguments();
      for (int i = 0;i < typeArguments.length;i++) {
        if (i > 0) {
          sb.append(',');
        }
        format(typeArguments[i], sb);
      }
      sb.append('>');
    } else if (type instanceof GenericArrayType) {
      GenericArrayType arrayType = (GenericArrayType)type;
      format(arrayType.getGenericComponentType(), sb);
      sb.append("[]");
    } else {
      throw new UnsupportedOperationException("todo " + type);
    }
  }

  public MethodHandle(String type, String name) {
    this.type = type;
    this.name = name;
    this.parameters = EMPTY_STRINGS;
  }

  public MethodHandle(String type, String name, String... parameters) {
    this.type = type;
    this.name = name;
    this.parameters = parameters.length == 0 ? parameters : parameters.clone();
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public int getParameterSize() {
    return parameters.length;
  }

  public String getParameterAt(int index) throws IndexOutOfBoundsException {
    if (index < 0 || index > parameters.length) {
      throw new IndexOutOfBoundsException("Bad index " + index);
    }
    return parameters[index];
  }

  public Iterator<String> iterator() {
    return Tools.iterator(parameters);
  }

  @Override
  public int hashCode() {
    return type.hashCode() ^ name.hashCode() ^ Arrays.hashCode(parameters);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof MethodHandle) {
      MethodHandle that = (MethodHandle)obj;
      return type.equals(that.type) && name.equals(that.name) && Arrays.equals(parameters, that.parameters);
    }
    return false;
  }

  @Override
  public String toString() {
    if (toString == null) {
      toString = Tools.join(new StringBuilder().append(type).append('#').append(name).append('('), ',', parameters).append(')').toString();
    }
    return toString;
  }
}
