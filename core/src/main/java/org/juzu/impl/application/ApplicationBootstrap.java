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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
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

   public void start() throws ApplicationException
   {
      _start();
   }
   
   private <B, I> void _start() throws ApplicationException
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
            // Direct declaration
            bootstrap.declareBean(type, bean.getScope(), bean.getQualifiers(), null);
         }
         else if (ProviderFactory.class.isAssignableFrom(implementation))
         {
            // Instantiate provider factory
            ProviderFactory mp;
            try
            {
               mp = (ProviderFactory)implementation.newInstance();
            }
            catch (InstantiationException e)
            {
               throw new ApplicationException(e);
            }
            catch (IllegalAccessException e)
            {
               throw new UndeclaredThrowableException(e);
            }

            // Get provider from factory
            Provider provider;
            try
            {
               provider = mp.getProvider(type);
            }
            catch (Exception e)
            {
               throw new ApplicationException(e);
            }

            // Bind provider instance
            bootstrap.bindProvider(
               type,
               bean.getScope(),
               determineQualifiers(type, bean.getQualifiers(), provider.getClass()),
               provider);
         }
         else if (Provider.class.isAssignableFrom(implementation))
         {
            // Bind provider
            bootstrap.declareProvider(
               type,
               bean.getScope(),
               determineQualifiers(type, bean.getQualifiers(), implementation),
               (Class)implementation);
         }
         else
         {
            // Bean implementation declaration
            bootstrap.declareBean(
               (Class)type,
               bean.getScope(),
               bean.getQualifiers(),
               (Class)implementation);
         }
      }

      //
      InjectManager<B, I> manager;
      try
      {
         manager = bootstrap.create();
      }
      catch (Exception e)
      {
         throw new UnsupportedOperationException("handle me gracefully", e);
      }

      // Let the container create the application context bean
      ApplicationContext context = null;
      try
      {
         B contextBean = manager.resolveBean(ApplicationContext.class);
         I contextInstance = manager.create(contextBean);
         context = (ApplicationContext)manager.get(contextBean, contextInstance);
      }
      catch (InvocationTargetException e)
      {
         throw new UnsupportedOperationException("handle me gracefully", e);
      }

      //
      this.context = context;
   }

   private Collection<Annotation> determineQualifiers(Class<?> type, Collection<Annotation> qualifiers, Class<?> implementation)
   {
      Collection<Annotation> overridenQualifiers = null;
      try
      {
         Method get = implementation.getMethod("get");
         for (Annotation annotation : get.getAnnotations())
         {
            if (annotation.annotationType().getAnnotation(Qualifier.class) != null)
            {
               if (overridenQualifiers == null)
               {
                  overridenQualifiers = new ArrayList<Annotation>();
               }
               overridenQualifiers.add(annotation);
            }
         }
      }
      catch (NoSuchMethodException e)
      {
         throw new UndeclaredThrowableException(e);
      }

      // Override all qualifiers
      if (overridenQualifiers != null)
      {
         qualifiers = overridenQualifiers;
      }

      //
      return qualifiers;
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
