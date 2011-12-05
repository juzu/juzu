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

import org.juzu.impl.inject.Export;
import org.juzu.impl.request.Scope;
import org.juzu.impl.spi.inject.InjectManager;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

/**
 * Juzu CDI extension.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ExtensionImpl implements Extension
{

   /** . */
   private final CDIManager manager;

   public ExtensionImpl()
   {
      this.manager = CDIManager.boot.get();
   }

   <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat)
   {
      AnnotatedType<T> annotatedType = pat.getAnnotatedType();
      Class<?> type = annotatedType.getJavaClass();
      if (annotatedType.isAnnotationPresent(Export.class))
      {
         if (!manager.declaredBeans.contains(type))
         {
            pat.veto();
         }
      }

      //
      for (Class<?> boundBeanType : manager.boundBeans.keySet())
      {
         if (boundBeanType.isAssignableFrom(type))
         {
            pat.veto();
         }
      }
   }

   void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager)
   {
      Container container = Container.boot.get();

      //
      for (Scope scope : container.scopes)
      {
         event.addContext(new ContextImpl(container.scopeController, scope, scope.getAnnotationType()));
      }

      // Add the manager
      event.addBean(new InstanceBean(InjectManager.class, manager));

      // Add singletons
      for (AbstractBean bean : manager.boundBeans.values())
      {
         event.addBean(bean);
      }
   }
}
