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

package juzu.impl.inject;

import juzu.Scope;
import juzu.impl.inject.spi.Injector;

import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Describes a bean registered in an IOC container.
 * <p/>
 * The {@link #declaredType} type is the mandatory type exposed by the bean. The {@link #implementationType} type can be
 * optionally provided to specify the implementation type of the bean. When the implementation type implements the
 * {@link javax.inject.Provider} interface, the implementation....
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class BeanDescriptor {

  /** The bean declared type. */
  private final Class<?> declaredType;

  /** The bean scope. */
  private final Scope scope;

  /** The bean qualifiers. */
  private final List<Annotation> qualifiers;

  /** The bean implementation type. */
  private final Class<?> implementationType;

  private BeanDescriptor(
      Class<?> declaredType,
      Scope scope,
      List<Annotation> qualifiers,
      Class<?> implementationType) throws NullPointerException, IllegalArgumentException {
    if (declaredType == null) {
      throw new NullPointerException("No null declared type accepted");
    }
    if (qualifiers != null) {
      for (Annotation qualifier : qualifiers) {
        if (qualifier.annotationType().getAnnotation(Qualifier.class) == null) {
          throw new IllegalArgumentException("Qualifier annotation " + qualifier + " is not annotated with @Qualifier");
        }
      }
    }

    //
    this.declaredType = declaredType;
    this.scope = scope;
    this.qualifiers = qualifiers;
    this.implementationType = implementationType;
  }

  public static <T> BeanDescriptor createFromProviderType(
      final Class<T> declaredType,
      Scope scope,
      List<Annotation> qualifiers,
      final Class<? extends Provider<T>> implementationType) throws NullPointerException, IllegalArgumentException {
    return new BeanDescriptor(declaredType, scope, qualifiers, implementationType) {
      @Override
      public void bind(Injector builder) {
        // Bind provider
        builder.declareProvider(
            declaredType,
            getScope(),
            determineQualifiers(getQualifiers(), implementationType),
            implementationType);
      }
    };
  }

  public static <T> BeanDescriptor createFromProvider(
      final Class<T> declaredType,
      Scope scope,
      List<Annotation> qualifiers,
      final Provider<? extends T> implementationType) throws NullPointerException, IllegalArgumentException {
    return new BeanDescriptor(declaredType, scope, qualifiers, null) {
      @Override
      public void bind(Injector builder) {
        // Bind provider
        builder.bindProvider(
            declaredType,
            getScope(),
            determineQualifiers(getQualifiers(), implementationType.getClass()),
            implementationType);
      }
    };
  }

  public static <T> BeanDescriptor createFromImpl(
      final Class<T> declaredType,
      Scope scope,
      List<Annotation> qualifiers,
      final Class<? extends T> implementationType) throws NullPointerException, IllegalArgumentException {
    return new BeanDescriptor(declaredType, scope, qualifiers, implementationType) {
      @Override
      public void bind(Injector builder) {
        // Bean implementation declaration
        builder.declareBean(
            declaredType,
            getScope(),
            getQualifiers(),
            implementationType);
      }
    };
  }

  public static <T> BeanDescriptor createFromBean(
      final Class<T> declaredType,
      Scope scope,
      List<Annotation> qualifiers) throws NullPointerException, IllegalArgumentException {
    // Direct declaration
    return new BeanDescriptor(declaredType, scope, qualifiers, null) {
      @Override
      public void bind(Injector builder) {
        builder.declareBean(declaredType, getScope(), getQualifiers(), null);
      }
    };
  }

  public Class<?> getDeclaredType() {
    return declaredType;
  }

  public Scope getScope() {
    return scope;
  }

  public List<Annotation> getQualifiers() {
    return qualifiers;
  }

  public Class<?> getImplementationType() {
    return implementationType;
  }

  public abstract void bind(Injector builder);

  private static Collection<Annotation> determineQualifiers(Collection<Annotation> qualifiers, Class<?> implementation) {
    Collection<Annotation> overridenQualifiers = null;
    try {
      Method get = implementation.getMethod("get");
      for (Annotation annotation : get.getAnnotations()) {
        if (annotation.annotationType().getAnnotation(Qualifier.class) != null) {
          if (overridenQualifiers == null) {
            overridenQualifiers = new ArrayList<Annotation>();
          }
          overridenQualifiers.add(annotation);
        }
      }
    }
    catch (NoSuchMethodException e) {
      throw new UndeclaredThrowableException(e);
    }

    // Override all qualifiers
    if (overridenQualifiers != null) {
      qualifiers = overridenQualifiers;
    }

    //
    return qualifiers;
  }
}
