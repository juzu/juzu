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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Named;
import org.juzu.impl.inject.ScopeController;
import org.juzu.impl.request.Scope;
import org.juzu.impl.spi.inject.InjectManager;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class GuiceManager implements InjectManager<Provider, Object>
{

   /** . */
   private Injector injector;

   /** . */
   private final ClassLoader classLoader;

   /** . */
   private final Map<String, Key<?>> nameMap;

   public GuiceManager(final GuiceBootstrap bootstrap)
   {

      AbstractModule module = new AbstractModule()
      {
         @Override
         protected void configure()
         {
            // Bind guice scopes
            for (Scope scope : bootstrap.scopes)
            {
               bindScope(scope.getAnnotationType(), new GuiceScope(scope, ScopeController.INSTANCE));
            }

            // Bind beans
            for (BeanBinding beanBinding : bootstrap.bindings)
            {
               AnnotatedBindingBuilder a = bind(beanBinding.type);
               LinkedBindingBuilder b;

               if (beanBinding.qualifier != null)
               {
                  b = a.annotatedWith(beanBinding.qualifier);
               }
               else
               {
                  b = a;
               }
               ScopedBindingBuilder c;
               if (beanBinding.provider != null)
               {
                  c = b.toProvider(beanBinding.provider);
               }
               else
               {
                  if (beanBinding.qualifier != null)
                  {
                     if (beanBinding.implementationType != null)
                     {
                        c = b.to(beanBinding.implementationType);
                     }
                     else
                     {
                        c = b.to(beanBinding.type);
                     }
                  }
                  else
                  {
                     if (beanBinding.implementationType != null)
                     {
                        c = b.to(beanBinding.implementationType);
                     }
                     else
                     {
                        c = b;
                     }
                  }
               }
               if (beanBinding.scope != null)
               {
                  c.in(beanBinding.scope.annotationType());
               }
            }

            // Bind the manager itself
            bind(InjectManager.class).toInstance(GuiceManager.this);

            // Bind singletons
            for (Map.Entry<Class<?>, Object> entry : bootstrap.singletons.entrySet())
            {
               bind((Class<Object>)entry.getKey(), entry.getValue());
            }
         }

         private <T> void bind(Class<T> clazz, T instance)
         {
            bind(clazz).toInstance(instance);
         }
      };

      //
      Map<String, Key<?>> nameMap = new HashMap<String, Key<?>>();
      Injector injector = Guice.createInjector(module);
      for (Key<?> key : injector.getBindings().keySet())
      {
         Class<? extends Annotation> annotationType = key.getAnnotationType();
         if (annotationType != null && Named.class.isAssignableFrom(annotationType))
         {
            Named named = (Named)key.getAnnotation();
            nameMap.put(named.value(), key) ;
         }
      }

      //
      this.injector = injector;
      this.nameMap = nameMap;
      this.classLoader = bootstrap.classLoader;
   }

   public String getImplementation()
   {
      return "inject/guice";
   }

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   public Provider resolveBean(Class<?> type)
   {
      return injector.getProvider(type);
   }

   public Provider resolveBean(String name)
   {
      Key<?> key = nameMap.get(name);
      return key != null ? injector.getProvider(key) : null;
   }

   public Object create(Provider bean)
   {
      return bean.get();
   }

   public void release(Object instance)
   {
   }

   public Object get(Provider bean, Object instance)
   {
      return instance;
   }
}
