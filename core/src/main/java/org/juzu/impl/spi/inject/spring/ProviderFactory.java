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

package org.juzu.impl.spi.inject.spring;

import org.springframework.beans.factory.FactoryBean;

import javax.inject.Provider;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class ProviderFactory<T> implements FactoryBean<T>
{

   /** . */
   private final Class<T> type;

   /** . */
   private final Provider<T> provider;

   ProviderFactory(Class<T> type, Provider<T> provider)
   {
      this.type = type;
      this.provider = provider;
   }

   public T getObject() throws Exception
   {
      return provider.get();
   }

   public Class<?> getObjectType()
   {
      return type;
   }

   public boolean isSingleton()
   {
      return false;
   }
}
