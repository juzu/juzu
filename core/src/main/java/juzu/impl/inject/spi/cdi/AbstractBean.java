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

package juzu.impl.inject.spi.cdi;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class AbstractBean implements Bean {

  /** . */
  static final Annotation DEFAULT_QUALIFIER = new AnnotationLiteral<Default>() {
  };

  /** . */
  static final Annotation ANY_QUALIFIER = new AnnotationLiteral<Any>() {
  };

  /** . */
  protected final Class<?> type;

  /** . */
  protected final Set<Annotation> qualifiers;

  /** . */
  private Class<? extends Annotation> scope;

  /** . */
  private final Set<Type> types;

  /** . */
  protected BeanManager manager;

  /** . */
  private String name;

  AbstractBean(Class<?> type, juzu.Scope scope, Iterable<Annotation> qualifiers) {
    HashSet<Type> types = new HashSet<Type>();
    collectSuperTypes(type, types);

    // Determine scope
    Class<? extends Annotation> scopeAnnotation = null;
    if (scope != null) {
      scopeAnnotation = scope.getAnnotationType();
    }
    else {
      for (Annotation annotation : type.getAnnotations()) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        if (annotationType.getAnnotation(Scope.class) != null) {
          scopeAnnotation = annotationType;
          break;
        }
      }
      if (scopeAnnotation == null) {
        scopeAnnotation = Dependent.class;
      }
    }

    // Build the qualifier map
    Map<Class<? extends Annotation>, Annotation> qualifierMap = new HashMap<Class<? extends Annotation>, Annotation>();
    if (qualifiers != null) {
      for (Annotation qualifier : qualifiers) {
        qualifierMap.put(qualifier.annotationType(), qualifier);
      }
    }
    else {
      // Introspect qualifiers if the qualifiers were not set
      for (Annotation annotation : type.getAnnotations()) {
        if (annotation.annotationType().getAnnotation(Qualifier.class) != null) {
          qualifierMap.put(annotation.annotationType(), annotation);
        }
      }
    }
    qualifierMap.put(Default.class, AbstractBean.DEFAULT_QUALIFIER);
    qualifierMap.put(Any.class, AbstractBean.ANY_QUALIFIER);

    //
    String name = null;
    Annotation named = qualifierMap.get(Named.class);
    if (named != null) {
      name = ((Named)named).value();
    }

    //
    this.type = type;
    this.types = types;
    this.qualifiers = Collections.unmodifiableSet(new HashSet<Annotation>(qualifierMap.values()));
    this.name = name;
    this.scope = scopeAnnotation;
  }

  private void collectSuperTypes(Class<?> type, HashSet<Type> superTypes) {
    superTypes.add(type);
    Class<?> superClassType = type.getSuperclass();
    if (superClassType != null) {
      collectSuperTypes(superClassType, superTypes);
    }
    for (Class<?> interfaceType : type.getInterfaces()) {
      collectSuperTypes(interfaceType, superTypes);
    }
  }

  void register(BeanManager manager) {
    this.manager = manager;
  }

  public final Set<Type> getTypes() {
    return types;
  }

  public final Class<? extends Annotation> getScope() {
    return scope;
  }

  public final Set<Annotation> getQualifiers() {
    return qualifiers;
  }

  public final String getName() {
    return name;
  }

  public final Set<Class<? extends Annotation>> getStereotypes() {
    return Collections.emptySet();
  }

  public final Class<?> getBeanClass() {
    return type;
  }

  public final boolean isAlternative() {
    return false;
  }

  public final boolean isNullable() {
    return false;
  }

  public final Set<InjectionPoint> getInjectionPoints() {
    return Collections.emptySet();
  }
}
