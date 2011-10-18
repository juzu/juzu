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

package org.juzu.impl.application;

import org.juzu.application.ApplicationDescriptor;
import org.juzu.impl.spi.cdi.Container;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Bootstrap
{

   /** . */
   static final ThreadLocal<Bootstrap> foo = new ThreadLocal<Bootstrap>();

   /** . */
   final Container container;

   /** . */
   final ApplicationDescriptor descriptor;

   /** . */
   private ApplicationContext context;

   public Bootstrap(Container container, ApplicationDescriptor descriptor)
   {
      this.container = container;
      this.descriptor = descriptor;
   }

   public void start() throws Exception
   {
      foo.set(this);
      try
      {
         container.start();

         // Make the bean available and force bean creation so it get
         // the thread local in this context
         BeanManager mgr = container.getManager();
         Bean bean = mgr.getBeans(ApplicationContext.class).iterator().next();
         CreationalContext<?> cc = mgr.createCreationalContext(bean);
         this.context = (ApplicationContext)mgr.getReference(bean, ApplicationContext.class, cc);
      }
      finally
      {
         foo.set(null);
      }
   }

   public ApplicationContext getContext()
   {
      return context;
   }

   public void stop()
   {
      container.stop();
   }
}
