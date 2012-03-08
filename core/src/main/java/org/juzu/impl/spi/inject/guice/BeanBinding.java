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

package org.juzu.impl.spi.inject.guice;

import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class BeanBinding<T>
{

   /** . */
   final Class<T> type;

   /** . */
   final Collection<Annotation> qualifiers;

   /** . */
   final Annotation scope;

   BeanBinding(Class<T> type, Class<? extends T> beanType, Iterable<Annotation> declaredQualifiers)
   {
      Annotation scope = null;
      Map<Class<?>, Annotation> qualifiers = null;
      if (declaredQualifiers != null)
      {
         for (Annotation declaredQualifier : declaredQualifiers)
         {
            if (qualifiers == null)
            {
               qualifiers = new HashMap<Class<?>, Annotation>();
            }
            qualifiers.put(declaredQualifier.annotationType(), declaredQualifier);
         }
      }
      if (beanType != null)
      {
         for (Annotation ann : beanType.getDeclaredAnnotations())
         {
            if (ann.annotationType().getAnnotation(Qualifier.class) != null)
            {
               if (qualifiers == null)
               {
                  qualifiers = new HashMap<Class<?>, Annotation>();
               }
               qualifiers.put(ann.annotationType(), ann);
            }
            if (ann.annotationType().getAnnotation(javax.inject.Scope.class) != null)
            {
               scope = ann;
            }
         }
      }

      //
      this.type = type;
      this.qualifiers = qualifiers != null ? qualifiers.values() : null;
      this.scope = scope;
   }

   static class ToType<T> extends BeanBinding<T>
   {

      /** . */
      final Class<? extends T> implementationType;

      ToType(Class<T> type, Class<? extends T> implementationType, Iterable<Annotation> declaredQualifiers)
      {
         super(type, implementationType != null ? implementationType : type, declaredQualifiers);

         //
         this.implementationType = implementationType;
      }
   }

   static class ToProviderType<T> extends BeanBinding<T>
   {

      /** . */
      final Class<? extends Provider<T>> provider;

      ToProviderType(Class<T> type, Class<? extends Provider<T>> provider, Iterable<Annotation> declaredQualifiers)
      {
         super(type, null, declaredQualifiers);

         //
         this.provider = provider;
      }
   }

   static class ToInstance<T> extends BeanBinding<T>
   {

      /** . */
      final T instance;

      ToInstance(Class<T> type, T instance, Iterable<Annotation> declaredQualifiers)
      {
         super(type, (Class<T>)instance.getClass(), declaredQualifiers);

         //
         this.instance = instance;
      }
   }

   static class ToProviderInstance<T> extends BeanBinding<T> implements com.google.inject.Provider<T>
   {

      /** . */
      final Provider<? extends T> provider;

      ToProviderInstance(Class<T> type, Provider<? extends T> provider)
      {
         super(type, null, null);

         //
         this.provider = provider;
      }

      public T get()
      {
         return provider.get();
      }
   }
}
