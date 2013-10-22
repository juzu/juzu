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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Provides a custom classloading policy oscillating between parent-first and child-first:
 * <ul>
 *   <li>a class is loaded by this loader when it exists and the same class in the parent loader does not
 *   exists or has a different bytecode</li>
 *   <li>a class is loaded bythe parent loaded when it does not exists in this loader or has the same bytecode</li>
 *   <li>classes loaded by ancestors loaders are not subject to this policy</li>
 * </ul>
 *
 * todo:
 * <ul>
 *   <li>implement getResources()</li>
 *   <li>Annotation scan</li>
 *   <li>ArrayType generics</li>
 *   <li>TypeVariable generics</li>
 *   <li>Bytecode analysis</li>
 * </ul>
 *
 *  @author Julien Viet
 */
public class LiveClassLoader extends URLClassLoader {

  /** . */
  private final ClassLoader parent;

  public LiveClassLoader(URL[] urls, ClassLoader parent) throws NullPointerException {
    super(urls, parent);

    //
    if (parent == null) {
      throw new NullPointerException("No null parent classloader accpeted");
    }

    //
    this.parent = parent;
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    Class<?> clazz = findLoadedClass(name);
    if (clazz == null) {
      try {
        clazz = parent.loadClass(name);
      }
      catch (ClassNotFoundException e) {
        return findClass(name);
      }
      if (loadLocally(clazz)) {
        clazz = findClass(name);
      }
      return clazz;
    } else {
      return clazz;
    }
  }

  private boolean loadLocally(Class<?> clazz) {
    return loadLocally(new LinkedList<Class<?>>(), clazz);
  }
  /**
   * Return true if the specified class should be loaded by this classloader (i.e with this same class).
   *
   * @param clazz the class to test
   * @return true if this class be be used
   */
  private boolean loadLocally(LinkedList<Class<?>> stack, Class<?> clazz) {
    if (clazz.getClassLoader() == this) {
      // That should not happen since the clazz argument is obtained from a class loader
      // by the parent classloader that is not aware of this classloader
      throw new UnsupportedOperationException("Attempt to determine loading of " + clazz.getName());
    } else if (clazz.getClassLoader() == parent) {
      if (stack.contains(clazz)) {
        return false;
      } else {
        stack.add(clazz);
        try {
          String resourceName = clazz.getName().replace('.', '/') + ".class";
          URL resource = findResource(resourceName);
          if (resource == null) {
            // No resource means it must be loaded from the parent
            return false;
          } else {
            URL parentResource = parent.getResource(resourceName);
            if (parentResource == null) {
              throw new UnsupportedOperationException("Could not find parent resource " + resourceName + " from parent loader");
            } else {
              try {
                byte[] parentBytes = Tools.bytes(parentResource);
                byte[] bytes = Tools.bytes(resource);
                if (Arrays.equals(parentBytes, bytes)) {
                  // If any one dependency could is not loaded locally then this clazz must not be loaded
                  // locally
                  for (Iterator<Class<?>> dependencies = getDirectDependencies(clazz);dependencies.hasNext();) {
                    Class<?> dependency = dependencies.next();
                    if (loadLocally(stack, dependency)) {
                      return true;
                    }
                  }
                  // Otherwise the parent can load it fine
                  stack.add(clazz);
                  return false;
                } else {
                  // We must load it locally as it has new bytecode
                  return true;
                }
              }
              catch (IOException e) {
                throw new UnsupportedOperationException("handle me gracefully", e);
              }
            }
          }
        }
        finally {
          stack.remove(clazz);
        }
      }
    } else {
      return false;
    }
  }

  private Iterator<Class<?>> getDirectDependencies(Class<?> type) {
    HashSet<Class<?>> dependencies = new HashSet<Class<?>>();
    resolveDirectDependencies(type, dependencies);
    return dependencies.iterator();
  }

  private void resolveDirectDependencies(Class<?> clazz, HashSet<Class<?>> dependencies) {
    for (Field field : clazz.getDeclaredFields()) {
      resolveGenericDirectDependencies(field.getGenericType(), dependencies);
    }
    for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
      for (Type genericParameterType : ctor.getGenericParameterTypes()) {
        resolveGenericDirectDependencies(genericParameterType, dependencies);
      }
    }
    for (Method method : clazz.getDeclaredMethods()) {
      resolveGenericDirectDependencies(method.getGenericReturnType(), dependencies);
      for (Type genericParameterType : method.getGenericParameterTypes()) {
        resolveGenericDirectDependencies(genericParameterType, dependencies);
      }
    }
    Type genericSuperClass = clazz.getGenericSuperclass();
    if (genericSuperClass != null) {
      resolveGenericDirectDependencies(genericSuperClass, dependencies);
    }
    for (Type genericInterface : clazz.getGenericInterfaces()) {
      resolveGenericDirectDependencies(genericInterface, dependencies);
    }
  }

  private void resolveGenericDirectDependencies(Type type, HashSet<Class<?>> dependencies) {
    if (type instanceof Class<?>) {
      dependencies.add((Class<?>)type);
    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType)type;
      for (Type typeArg : parameterizedType.getActualTypeArguments()) {
        resolveGenericDirectDependencies(typeArg, dependencies);
      }
    } else if (type instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType)type;
      for (Type upperBound : wildcardType.getUpperBounds()) {
        resolveGenericDirectDependencies(upperBound, dependencies);
      }
      for (Type lowerBound : wildcardType.getLowerBounds()) {
        resolveGenericDirectDependencies(lowerBound, dependencies);
      }
    } else {
      throw new UnsupportedOperationException("Type " + type + " not yet supported");
    }
  }

  @Override
  public URL getResource(String name) {
    URL url = findResource(name);
    if (url == null) {
      url = super.getResource(name);
    }
    return url;
  }
}
