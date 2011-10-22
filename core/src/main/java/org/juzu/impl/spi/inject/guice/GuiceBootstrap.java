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

import org.juzu.impl.request.Scope;
import org.juzu.impl.spi.inject.InjectBootstrap;
import org.juzu.impl.spi.inject.InjectManager;
import org.juzu.impl.spi.fs.ReadFileSystem;

import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class GuiceBootstrap extends InjectBootstrap
{

   /** . */
   final List<BeanBinding> bindings;

   /** . */
   final Set<Scope> scopes;

   /** . */
   final Map<Class<?>, Object> singletons;

   /** . */
   ClassLoader classLoader;

   public GuiceBootstrap()
   {
      this.bindings = new ArrayList<BeanBinding>();
      this.scopes = new HashSet<Scope>();
      this.singletons = new HashMap<Class<?>, Object>();
   }

   @Override
   public <T> InjectBootstrap declareBean(Class<T> type, Class<? extends T> implementationType)
   {
      add(type, implementationType, null);
      return this;
   }

   @Override
   public <T> InjectBootstrap declareProvider(Class<T> type, Class<? extends Provider<T>> provider)
   {
      add(type, null, provider);
      return this;
   }

   private <T> void add(Class<T> type, Class<? extends T> implementationType, Class<? extends Provider<T>> provider)
   {
      // Scope and qualifier discovery
      Annotation qualifier = null;
      Annotation scope = null;
      Class<?> foo = implementationType != null ? implementationType : type;
      for (Annotation ann : foo.getDeclaredAnnotations())
      {
         if (ann.annotationType().getAnnotation(Qualifier.class) != null)
         {
            qualifier = ann;
         }
         if (ann.annotationType().getAnnotation(javax.inject.Scope.class) != null)
         {
            scope = ann;
         }
      }

      //
      bindings.add(new BeanBinding<T>(type, implementationType, qualifier, scope, provider));
   }

   @Override
   public <P> InjectBootstrap addFileSystem(ReadFileSystem<P> fs)
   {
      return this;
   }

   @Override
   public InjectBootstrap addScope(Scope scope)
   {
      scopes.add(scope);
      return this;
   }

   @Override
   public InjectBootstrap setClassLoader(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
      return this;
   }

   @Override
   public <T> InjectBootstrap bindSingleton(Class<T> type, T instance)
   {
      singletons.put(type, instance);
      return this;
   }

   @Override
   public InjectManager<?, ?> create()
   {
      return new GuiceManager(this);
   }
}
