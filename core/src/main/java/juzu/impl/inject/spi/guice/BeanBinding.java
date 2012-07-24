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
