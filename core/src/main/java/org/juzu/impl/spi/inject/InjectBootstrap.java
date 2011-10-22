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

package org.juzu.impl.spi.inject;

import org.juzu.impl.request.Scope;
import org.juzu.impl.spi.fs.ReadFileSystem;

import javax.inject.Provider;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class InjectBootstrap
{

   public abstract <T> InjectBootstrap declareBean(Class<T> type, Class<? extends T> implementationType);

   public abstract <T> InjectBootstrap declareProvider(Class<T> type, Class<? extends Provider<T>> provider);

   public abstract <T> InjectBootstrap bindSingleton(Class<T> type, T instance);

   public abstract <P> InjectBootstrap addFileSystem(ReadFileSystem<P> fs);

   public abstract InjectBootstrap addScope(Scope scope);

   public abstract InjectBootstrap setClassLoader(ClassLoader classLoader);

   public abstract <B, I> InjectManager<B, I> create() throws Exception;

}
