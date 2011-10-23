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

package org.juzu.impl.cdi;

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
public class JuzuExt implements Extension
{

   public JuzuExt()
   {
   }

   <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat)
   {
      AnnotatedType<T> annotatedType = pat.getAnnotatedType();
      Class<?> type = annotatedType.getJavaClass();
      if (type.getName().startsWith("org.juzu."))
      {
         boolean present = annotatedType.isAnnotationPresent(Export.class);
         if (!present)
         {
            pat.veto();
         }
      }
   }

   void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager)
   {
      event.addContext(ScopeController.INSTANCE.flashContext);
      event.addContext(ScopeController.INSTANCE.requestContext);
      event.addContext(ScopeController.INSTANCE.sessionContext);
   }
}
