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

import org.juzu.Action;
import org.juzu.Resource;
import org.juzu.Scope;
import org.juzu.View;
import org.juzu.impl.application.metadata.ApplicationDescriptor;
import org.juzu.impl.inject.BeanFilter;
import org.juzu.impl.metadata.BeanDescriptor;
import org.juzu.impl.inject.Export;
import org.juzu.inject.ProviderFactory;
import org.juzu.impl.spi.inject.InjectBuilder;
import org.juzu.impl.spi.inject.InjectManager;

import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationBootstrap
{

   /** . */
   public final InjectBuilder bootstrap;

   /** . */
   public final ApplicationDescriptor descriptor;

   /** . */
   private ApplicationContext context;

   public ApplicationBootstrap(InjectBuilder bootstrap, ApplicationDescriptor descriptor)
   {
      this.bootstrap = bootstrap;
      this.descriptor = descriptor;
   }

   public void start() throws Exception
   {
      _start();
   }
   
   private <B, I> void _start() throws Exception
   {
      // Bind the application descriptor
      bootstrap.bindBean(ApplicationDescriptor.class, null, descriptor);

      // Bind the application context
      bootstrap.declareBean(ApplicationContext.class, null, null, null);

      //
      bootstrap.setFilter(new BeanFilter()
      {
         public <T> boolean acceptBean(Class<T> beanType)
         {
            if (beanType.getName().startsWith("org.juzu.") || beanType.getAnnotation(Export.class) != null)
            {
               return false;
            }
            else
            {
               // Do that better with a meta annotation that describe Juzu annotation
               // that veto beans
               for (Method method : beanType.getMethods())
               {
                  if (method.getAnnotation(View.class) != null || method.getAnnotation(Action.class) != null || method.getAnnotation(Resource.class) != null)
                  {
                     return false;
                  }
               }
               return true;
            }
         }
      });

      // Bind the scopes
      for (Scope scope : Scope.values())
      {
         bootstrap.addScope(scope);
      }

      // Bind the beans
      for (BeanDescriptor bean : descriptor.getBeans())
      {
         Class<?> type = bean.getDeclaredType();
         Class<?> implementation = bean.getImplementationType();
         if (implementation == null)
         {
            bootstrap.declareBean(type, bean.getScope(), bean.getQualifiers(), null);
         }
         else if (ProviderFactory.class.isAssignableFrom(implementation))
         {
            ProviderFactory mp = (ProviderFactory)implementation.newInstance();
            Provider provider = mp.getProvider(type);
            bootstrap.bindProvider(type, bean.getScope(), bean.getQualifiers(), provider);
         }
         else if (Provider.class.isAssignableFrom(implementation))
         {
            Method m = implementation.getMethod("get");
            Collection<Annotation> qualifiers = bean.getQualifiers();
            for (Annotation annotation : m.getAnnotations())
            {
               if (annotation.annotationType().getAnnotation(Qualifier.class) != null)
               {
                  if (qualifiers == null)
                  {
                     qualifiers = new ArrayList<Annotation>();
                  }
                  qualifiers.add(annotation);
               }
            }
            bootstrap.declareProvider(type, bean.getScope(), qualifiers, (Class)implementation);
         }
         else
         {
            bootstrap.declareBean((Class)type, bean.getScope(), bean.getQualifiers(), (Class)implementation);
         }
      }

      //
      InjectManager<B, I> manager = bootstrap.create();

      //
      B contextBean = manager.resolveBean(ApplicationContext.class);
      I contextInstance = manager.create(contextBean);
      
      //
      this.context = (ApplicationContext)manager.get(contextBean, contextInstance);
   }
   
   public ApplicationContext getContext()
   {
      return context;
   }

   public void stop()
   {
      // container.stop();
   }
}
