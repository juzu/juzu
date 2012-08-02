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

package juzu.impl.inject;

import juzu.Scope;
import juzu.impl.plugin.application.ApplicationException;
import juzu.impl.inject.spi.InjectBuilder;
import juzu.inject.ProviderFactory;

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
public final class BeanDescriptor {

  /** The bean declared type. */
  private final Class<?> declaredType;

  /** The bean scope. */
  private final Scope scope;

  /** The bean qualifiers. */
  private final List<Annotation> qualifiers;

  /** The bean implementation type. */
  private final Class<?> implementationType;

  public BeanDescriptor(
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

  public void install(InjectBuilder builder) {
    Class<?> type = getDeclaredType();
    Class<?> implementation = getImplementationType();
    if (implementation == null) {
      // Direct declaration
      builder.declareBean(type, getScope(), getQualifiers(), null);
    }
    else if (ProviderFactory.class.isAssignableFrom(implementation)) {
      // Instantiate provider factory
      ProviderFactory mp;
      try {
        mp = (ProviderFactory)implementation.newInstance();
      }
      catch (InstantiationException e) {
        throw new ApplicationException(e);
      }
      catch (IllegalAccessException e) {
        throw new UndeclaredThrowableException(e);
      }

      // Get provider from factory
      Provider provider;
      try {
        provider = mp.getProvider(type);
      }
      catch (Exception e) {
        throw new ApplicationException(e);
      }

      // Bind provider instance
      builder.bindProvider(
        type,
        getScope(),
        determineQualifiers(getQualifiers(), provider.getClass()),
        provider);
    }
    else if (Provider.class.isAssignableFrom(implementation)) {
      // Bind provider
      builder.declareProvider(
        type,
        getScope(),
        determineQualifiers(getQualifiers(), implementation),
        (Class)implementation);
    }
    else {
      // Bean implementation declaration
      builder.declareBean(
        (Class)type,
        getScope(),
        getQualifiers(),
        (Class)implementation);
    }
  }

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
