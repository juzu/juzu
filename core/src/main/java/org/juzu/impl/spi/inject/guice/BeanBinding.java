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

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class BeanBinding<T>
{

   /** . */
   final Class<T> type;

   /** . */
   final Annotation qualifier;

   /** . */
   final Annotation scope;

   BeanBinding(Class<T> type, Class<? extends T> beanType)
   {
      Annotation qualifier = null;
      Annotation scope = null;
      if (beanType != null)
      {
         for (Annotation ann : beanType.getDeclaredAnnotations())
         {
            if (ann.annotationType().getAnnotation(Qualifier.class) != null)
            {
               qualifier = ann;
            }
            if (ann.annotationType().getAnnotation(javax.inject.Scope.class) != null)
            {
               scope = ann;
            }
         }
      }

      //
      this.type = type;
      this.qualifier = qualifier;
      this.scope = scope;
   }

   static class ToType<T> extends BeanBinding<T>
   {

      /** . */
      final Class<? extends T> implementationType;

      ToType(Class<T> type, Class<? extends T> implementationType)
      {
         super(type, implementationType != null ? implementationType : type);

         //
         this.implementationType = implementationType;
      }
   }

   static class ToProviderType<T> extends BeanBinding<T>
   {

      /** . */
      final Class<? extends Provider<T>> provider;

      ToProviderType(Class<T> type, Class<? extends Provider<T>> provider)
      {
         super(type, null);

         //
         this.provider = provider;
      }
   }

   static class ToInstance<T> extends BeanBinding<T>
   {

      /** . */
      final T instance;

      ToInstance(Class<T> type, T instance)
      {
         super(type, (Class<T>)instance.getClass());

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
         super(type, null);

         //
         this.provider = provider;
      }

      public T get()
      {
         return provider.get();
      }
   }
}
