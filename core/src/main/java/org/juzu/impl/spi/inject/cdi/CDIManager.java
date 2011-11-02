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

import org.juzu.AmbiguousResolutionException;
import org.juzu.impl.spi.inject.InjectManager;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CDIManager implements InjectManager<Bean<?>, CreationalContext<?>>
{

   /** . */
   static final ThreadLocal<CDIManager> boot = new ThreadLocal<CDIManager>();

   /** . */
   private BeanManager manager;

   /** . */
   final Map<Class<?>, Object> singletons;

   /** . */
   final ClassLoader classLoader;

   /** . */
   final Set<Class<?>> beans;

   public CDIManager(Container container, Map<Class<?>, Object> singletons, Set<Class<?>> beans) throws Exception
   {
      this.singletons = singletons;
      this.beans = beans;

      //
      boot.set(this);
      try
      {
         container.start();
      }
      finally
      {
         boot.set(null);
      }

      //
      this.classLoader = container.getClassLoader();
      this.manager = container.getManager();
   }

   public String getImplementation()
   {
      return "cdi/weld";
   }

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   public Bean<?> resolveBean(Class<?> type)
   {
      Set<Bean<?>> beans = manager.getBeans(type);
      switch (beans.size())
      {
         case 0:
            return null;
         case 1:
            return beans.iterator().next();
         default:
            throw new AmbiguousResolutionException("Could not resolve bean of type " + type + ": " + beans);
      }
   }

   public Bean<?> resolveBean(String name)
   {
      Set<Bean<?>> beans = manager.getBeans(name);
      switch (beans.size())
      {
         case 0:
            return null;
         case 1:
            return beans.iterator().next();
         default:
            throw new AmbiguousResolutionException("Could not resolve bean of type " + name + ": " + beans);
      }
   }

   public CreationalContext<?> create(Bean<?> bean)
   {
      return manager.createCreationalContext(bean);
   }

   public void release(CreationalContext<?> instance)
   {
      instance.release();
   }

   public Object get(Bean<?> bean, CreationalContext<?> instance)
   {
      return manager.getReference(bean, bean.getBeanClass(), instance);
   }
}
