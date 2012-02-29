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
import org.juzu.impl.spi.inject.InjectBuilder;
import org.juzu.impl.spi.inject.InjectManager;
import org.juzu.impl.spi.fs.ReadFileSystem;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class GuiceBootstrap extends InjectBuilder
{

   /** . */
   final List<BeanBinding> bindings;

   /** . */
   final Set<Scope> scopes;

   /** . */
   ClassLoader classLoader;

   public GuiceBootstrap()
   {
      this.bindings = new ArrayList<BeanBinding>();
      this.scopes = new HashSet<Scope>();
   }

   @Override
   public <T> InjectBuilder declareBean(Class<T> type, Class<? extends T> implementationType)
   {
      bindings.add(new BeanBinding.ToType<T>(type, implementationType));
      return this;
   }

   @Override
   public <T> InjectBuilder declareProvider(Class<T> type, Class<? extends Provider<T>> provider)
   {
      bindings.add(new BeanBinding.ToProviderType<T>(type, provider));
      return this;
   }

   @Override
   public <P> InjectBuilder addFileSystem(ReadFileSystem<P> fs)
   {
      return this;
   }

   @Override
   public InjectBuilder addScope(Scope scope)
   {
      scopes.add(scope);
      return this;
   }

   @Override
   public InjectBuilder setClassLoader(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
      return this;
   }

   @Override
   public <T> InjectBuilder bindBean(Class<T> type, Iterable<Annotation> qualifiers, T instance)
   {
      bindings.add(new BeanBinding.ToInstance<T>(type, instance, qualifiers));
      return this;
   }

   @Override
   public <T> InjectBuilder bindProvider(Class<T> type, Provider<T> provider)
   {
      bindings.add(new BeanBinding.ToProviderInstance<T>(type, provider));
      return this;
   }

   @Override
   public InjectManager<?, ?> create()
   {
      return new GuiceManager(this);
   }
}
