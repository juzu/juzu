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

package juzu.impl.inject.spi.guice;

import juzu.Scope;

import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class BeanBinding<T> {

  /** . */
  final Class<T> type;

  /** . */
  final Collection<Annotation> qualifiers;

  /** . */
  final Class<? extends Annotation> scopeType;

  BeanBinding(Class<T> type, Scope scope, Iterable<Annotation> declaredQualifiers, Class<? extends T> beanType) {
    Class<? extends Annotation> scopeType = null;
    Map<Class<?>, Annotation> qualifiers = null;
    if (declaredQualifiers != null) {
      for (Annotation declaredQualifier : declaredQualifiers) {
        if (qualifiers == null) {
          qualifiers = new HashMap<Class<?>, Annotation>();
        }
        qualifiers.put(declaredQualifier.annotationType(), declaredQualifier);
      }
    }
    if (beanType != null) {
      for (Annotation ann : beanType.getDeclaredAnnotations()) {
        if (ann.annotationType().getAnnotation(Qualifier.class) != null) {
          if (qualifiers == null) {
            qualifiers = new HashMap<Class<?>, Annotation>();
          }
          qualifiers.put(ann.annotationType(), ann);
        }
        if (ann.annotationType().getAnnotation(javax.inject.Scope.class) != null) {
          scopeType = ann.annotationType();
        }
      }
    }

    //
    if (scope != null) {
      scopeType = scope.getAnnotationType();
    }

    //
    this.type = type;
    this.qualifiers = qualifiers != null ? qualifiers.values() : null;
    this.scopeType = scopeType;
  }

  static class ToType<T> extends BeanBinding<T> {

    /** . */
    final Class<? extends T> implementationType;

    ToType(Class<T> type, Scope scope, Iterable<Annotation> declaredQualifiers, Class<? extends T> implementationType) {
      super(type, scope, declaredQualifiers, implementationType != null ? implementationType : type);

      //
      this.implementationType = implementationType;
    }
  }

  static class ToProviderType<T> extends BeanBinding<T> {

    /** . */
    final Class<? extends Provider<T>> provider;

    ToProviderType(Class<T> type, Scope scope, Iterable<Annotation> declaredQualifiers, Class<? extends Provider<T>> provider) {
      super(type, scope, declaredQualifiers, null);

      //
      this.provider = provider;
    }
  }

  static class ToInstance<T> extends BeanBinding<T> {

    /** . */
    final T instance;

    ToInstance(Class<T> type, Iterable<Annotation> declaredQualifiers, T instance) {
      super(type, null, declaredQualifiers, (Class<T>)instance.getClass());

      //
      this.instance = instance;
    }
  }

  static class ToProviderInstance<T> extends BeanBinding<T> implements com.google.inject.Provider<T> {

    /** . */
    final Provider<? extends T> provider;

    ToProviderInstance(Class<T> type, Scope scope, Iterable<Annotation> declaredQualifiers, Provider<? extends T> provider) {
      super(type, scope, declaredQualifiers, null);

      //
      this.provider = provider;
    }

    public T get() {
      return provider.get();
    }
  }
}
