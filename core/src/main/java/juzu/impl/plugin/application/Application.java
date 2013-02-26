/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package juzu.impl.plugin.application;

import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.inject.spi.InjectionContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Singleton
public class Application {

  /** . */
  private final ApplicationDescriptor descriptor;

  /** . */
  final InjectionContext<?, ?> injectionContext;

  @Inject
  public Application(InjectionContext injectionContext, ApplicationDescriptor descriptor) throws Exception {
    this.injectionContext = injectionContext;
    this.descriptor = descriptor;
  }

  public String getName() {
    return descriptor.getName();
  }

  public ClassLoader getClassLoader() {
    return injectionContext.getClassLoader();
  }

  public ApplicationDescriptor getDescriptor() {
    return descriptor;
  }

  public Object resolveBean(String name) throws InvocationTargetException {
    return resolveBean(injectionContext, name);
  }

  private <B, I> Object resolveBean(InjectionContext<B, I> manager, String name) throws InvocationTargetException {
    B bean = manager.resolveBean(name);
    if (bean != null) {
      I cc = manager.create(bean);
      return manager.get(bean, cc);
    }
    else {
      return null;
    }
  }
}
