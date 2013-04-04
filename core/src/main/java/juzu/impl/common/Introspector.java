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

package juzu.impl.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Introspector {

  public static Class<?> resolveToClass(Type implementation, Class<?> type, int parameterIndex) {
    if (implementation == null) {
      throw new NullPointerException("No null type accepted");
    }

    // First resolve to type
    Type resolvedType = resolve(implementation, type, parameterIndex);

    //
    if (resolvedType != null) {
      return resolveToClass(resolvedType);
    } else {
      return null;
    }
  }

  public static Class resolveToClass(Type type) {
    if (type == null) {
      throw new NullPointerException("No null type accepted");
    }
    if (type instanceof Class<?>) {
      return (Class<?>)type;
    } else if (type instanceof TypeVariable) {
      TypeVariable resolvedTypeVariable = (TypeVariable)type;
      return resolveToClass(resolvedTypeVariable.getBounds()[0]);
    } else {
      throw new UnsupportedOperationException("Type resolution of " + type + " not yet implemented");
    }
  }

  /**
   * A simplistic implementation, it may not handle all cases but it should handle enough.
   *
   * @param implementation the type for which the parameter requires a resolution
   * @param type the type that owns the parameter
   * @param parameterIndex the parameter index
   * @return the resolved type
   */
  public static Type resolve(Type implementation, Class<?> type, int parameterIndex) {
    if (implementation == null) {
      throw new NullPointerException();
    }

    //
    if (implementation == type) {
      TypeVariable<? extends Class<?>>[] tp = type.getTypeParameters();
      if (parameterIndex < tp.length) {
        return tp[parameterIndex];
      } else {
        throw new IllegalArgumentException();
      }
    } else if (implementation instanceof Class<?>) {
      Class<?> c = (Class<?>) implementation;
      Type gsc = c.getGenericSuperclass();
      Type resolved = null;
      if (gsc != null) {
        resolved = resolve(gsc, type, parameterIndex);
        if (resolved == null) {
          // Try with interface
        }
      }
      return resolved;
    } else if (implementation instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) implementation;
      Type[] typeArgs = pt.getActualTypeArguments();
      Type rawType = pt.getRawType();
      if (rawType == type) {
        return typeArgs[parameterIndex];
      } else if (rawType instanceof Class<?>) {
        Class<?> classRawType = (Class<?>)rawType;
        Type resolved = resolve(classRawType, type, parameterIndex);
        if (resolved == null) {
          return null;
        } else if (resolved instanceof TypeVariable) {
          TypeVariable resolvedTV = (TypeVariable)resolved;
          TypeVariable[] a = classRawType.getTypeParameters();
          for (int i = 0;i < a.length;i++) {
            if (a[i].equals(resolvedTV)) {
              return resolve(implementation, classRawType, i);
            }
          }
          throw new AssertionError();
        } else {
          throw new UnsupportedOperationException("Cannot support resolution of " + resolved);
        }
      } else {
        throw new UnsupportedOperationException();
      }
    } else {
      throw new UnsupportedOperationException("todo " + implementation + " " + implementation.getClass());
    }
  }

  public static boolean instanceOf(Class c, List<String> types) {

    for (String type: types) {
      if (instanceOf(c, type)) {
        return true;
      }
    }

    return false;

  }


  public static boolean instanceOf(Class c, String type) {

    if (c.getName().equals(type)) {
      return true;
    }

    for (Class i : c.getInterfaces()) {
      if (instanceOf(i, type)) {
        return true;
      }
    }

    if (c.getSuperclass() != null) {
      return instanceOf(c.getSuperclass(), type);
    }

    return false;
  }

}
