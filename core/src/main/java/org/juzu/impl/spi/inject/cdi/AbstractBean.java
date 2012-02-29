/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.juzu.impl.spi.inject.cdi;

import org.springframework.expression.spel.ast.QualifiedIdentifier;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class AbstractBean implements Bean
{

   /** . */
   private final Class<?> type;

   /** . */
   private final Set<Annotation> qualifiers;

   /** . */
   private Set<Type> types;

   AbstractBean(Class<?> type)
   {
      Set<Annotation> qualifiers = new HashSet<Annotation>();
      qualifiers.add( new AnnotationLiteral<Default>() {} );
      qualifiers.add( new AnnotationLiteral<Any>() {} );
      for (Annotation annotation : type.getAnnotations())
      {
         if (annotation.annotationType().getAnnotation(Qualifier.class) != null)
         {
            qualifiers.add(annotation);
         }
      }

      //
      HashSet<Type> types = new HashSet<Type>();
      collectSuperTypes(type, types);

      //
      this.type = type;
      this.types = types;
      this.qualifiers = Collections.unmodifiableSet(qualifiers);
   }

   private void collectSuperTypes(Class<?> type, HashSet<Type> superTypes)
   {
      superTypes.add(type);
      Class<?> superClassType = type.getSuperclass();
      if (superClassType != null)
      {
         collectSuperTypes(superClassType, superTypes);
      }
      for (Class<?> interfaceType : type.getInterfaces())
      {
         collectSuperTypes(interfaceType, superTypes);
      }
   }

   public Set<Type> getTypes()
   {
      return types;
   }

   public Set<Annotation> getQualifiers()
   {
      return qualifiers;
   }

   public Class<? extends Annotation> getScope()
   {
      return Singleton.class;
   }

   public String getName()
   {
      return null;
   }

   public Set<Class<? extends Annotation>> getStereotypes()
   {
      return Collections.emptySet();
   }

   public Class<?> getBeanClass()
   {
      return type;
   }

   public boolean isAlternative()
   {
      return false;
   }

   public boolean isNullable()
   {
      return false;
   }

   public Set<InjectionPoint> getInjectionPoints()
   {
      return Collections.emptySet();
   }

   public void destroy(Object instance, CreationalContext ctx)
   {
      ctx.release();
   }
}
