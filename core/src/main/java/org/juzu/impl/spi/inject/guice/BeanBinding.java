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
import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class BeanBinding<T>
{

   /** . */
   final Class<T> type;

   /** . */
   final Class<? extends T> implementationType;

   /** . */
   final Annotation qualifier;

   /** . */
   final Annotation scope;

   /** . */
   final Class<? extends Provider<T>> provider;

   BeanBinding(Class<T> type, Class<? extends T> implementationType, Annotation qualifier, Annotation scope, Class<? extends Provider<T>> provider)
   {
      this.type = type;
      this.implementationType = implementationType;
      this.qualifier = qualifier;
      this.scope = scope;
      this.provider = provider;
   }
}
