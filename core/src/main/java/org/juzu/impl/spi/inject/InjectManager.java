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

package org.juzu.impl.spi.inject;

import java.lang.reflect.InvocationTargetException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface InjectManager<B, I>
{

   String getImplementation();

   ClassLoader getClassLoader();

   B resolveBean(Class<?> type);

   B resolveBean(String name);

   void release(I instance);

   /**
    * Create a bean instance for the specified bean.
    *
    * @param bean the bean
    * @return the bean instance
    * @throws InvocationTargetException wrap any exception throws,by the bean class during its creation.
    */
   I create(B bean) throws InvocationTargetException;

   /**
    * Get the bean object associated the bean instance.
    *
    * @param bean the bean
    * @param instance the bean instance
    * @return the bean instance
    * @throws InvocationTargetException wrap any exception throws,by the bean class during its creation.
    */
   Object get(B bean, I instance) throws InvocationTargetException;

}
