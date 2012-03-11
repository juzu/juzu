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
import org.juzu.View;
import org.juzu.impl.application.metadata.ApplicationDescriptor;
import org.juzu.impl.inject.BeanFilter;
import org.juzu.impl.metadata.BeanDescriptor;
import org.juzu.inject.Binding;
import org.juzu.inject.Bindings;
import org.juzu.impl.inject.Export;
import org.juzu.impl.inject.MetaProvider;
import org.juzu.impl.request.Scope;
import org.juzu.impl.spi.inject.InjectBuilder;
import org.juzu.impl.spi.inject.InjectManager;
import org.juzu.plugin.Plugin;

import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationBootstrap
{

   /** . */
   public final InjectBuilder bootstrap;

   /** . */
   public final ApplicationDescriptor descriptor;

   /** . */
   private InternalApplicationContext context;

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
      bootstrap.declareBean(ApplicationContext.class, null, InternalApplicationContext.class);

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

      // Bind the controllers
      for (BeanDescriptor bean : descriptor.getController().getBeans())
      {
         bootstrap.declareBean((Class)bean.getDeclaredType(), null, (Class)bean.getImplementationType());
      }

      // Bind the templates
      for (BeanDescriptor bean : descriptor.getTemplates().getBeans())
      {
         bootstrap.declareBean((Class)bean.getDeclaredType(), null, (Class)bean.getImplementationType());
      }

      //
      Class<?> s = descriptor.getPackageClass();

      // Bind the bean bindings
      Bindings bindings = s.getAnnotation(Bindings.class);
      if (bindings != null)
      {
         for (Binding binding : bindings.value())
         {
            Class<?> type = binding.value();
            Class<?> implementation = binding.implementation();
            if (MetaProvider.class.isAssignableFrom(implementation))
            {
               MetaProvider mp = (MetaProvider)implementation.newInstance();
               Provider provider = mp.getProvider(type);
               bootstrap.bindProvider(type, null, provider);
            }
            else if (Provider.class.isAssignableFrom(implementation))
            {
               Method m = implementation.getMethod("get");
               ArrayList<Annotation> qualifiers = null;
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
               bootstrap.declareProvider(type, qualifiers, (Class)implementation);
            }
            else
            {
               if (implementation == Object.class)
               {
                  implementation = null;
               }
               bootstrap.declareBean((Class)type, null, (Class)implementation);
            }
         }
      }

      //
      List<Class<? extends Plugin>> plugins = descriptor.getPlugins();

      // Declare the plugins
      for (Class<? extends Plugin> pluginType : plugins)
      {
         bootstrap.declareBean(pluginType, null, null);
      }

      //
      InjectManager<B, I> manager = bootstrap.create();

      //
      B contextBean = manager.resolveBean(ApplicationContext.class);
      I contextInstance = manager.create(contextBean);
      
      //
      this.context = (InternalApplicationContext)manager.get(contextBean, contextInstance);
   }
   
   public InternalApplicationContext getContext()
   {
      return context;
   }

   public void stop()
   {
      // container.stop();
   }
}
