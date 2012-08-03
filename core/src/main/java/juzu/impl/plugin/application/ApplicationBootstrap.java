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

import juzu.Action;
import juzu.Resource;
import juzu.Scope;
import juzu.View;
import juzu.impl.inject.BeanFilter;
import juzu.impl.inject.Export;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.inject.spi.BeanLifeCycle;
import juzu.impl.plugin.Plugin;
import juzu.impl.inject.spi.InjectBuilder;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationBootstrap {

  /** . */
  public final InjectBuilder injectBuilder;

  /** . */
  public final ApplicationDescriptor descriptor;

  /** . */
  private BeanLifeCycle<ApplicationContext> contextLifeCycle;

  /** . */
  private InjectionContext<?, ?> injectionContext;

  public ApplicationBootstrap(InjectBuilder injectBuilder, ApplicationDescriptor descriptor) {
    this.injectBuilder = injectBuilder;
    this.descriptor = descriptor;
  }

  public void start() throws ApplicationException {
    _start();
  }

  private <B, I> void _start() throws ApplicationException {
    // Bind the application descriptor
    injectBuilder.bindBean(ApplicationDescriptor.class, null, descriptor);

    // Bind the application context
    injectBuilder.declareBean(ApplicationContext.class, null, null, null);

    //
    injectBuilder.setFilter(new BeanFilter() {
      public <T> boolean acceptBean(Class<T> beanType) {
        if (beanType.getName().startsWith("juzu.") || beanType.getAnnotation(Export.class) != null) {
          return false;
        }
        else {
          // Do that better with a meta annotation that describe Juzu annotation
          // that veto beans
          for (Method method : beanType.getMethods()) {
            if (method.getAnnotation(View.class) != null || method.getAnnotation(Action.class) != null || method.getAnnotation(Resource.class) != null) {
              return false;
            }
          }
          return true;
        }
      }
    });

    // Bind the scopes
    for (Scope scope : Scope.values()) {
      injectBuilder.addScope(scope);
    }

    // Bind the plugins
    for (Plugin plugin : descriptor.getFoo().values()) {
      Class aClass = plugin.getClass();
      Object o = plugin;
      injectBuilder.bindBean(aClass, null, o);
    }

    // Bind the beans
    for (BeanDescriptor bean : descriptor.getBeans()) {
      bean.install(injectBuilder);
    }

    //
    InjectionContext<B, I> injectionContext;
    try {
      injectionContext = injectBuilder.create();
    }
    catch (Exception e) {
      throw new UnsupportedOperationException("handle me gracefully", e);
    }

    // Let the container create the application context bean
    BeanLifeCycle<ApplicationContext> contextLifeCycle = injectionContext.get(ApplicationContext.class);
    try {
      contextLifeCycle.get();
    }
    catch (InvocationTargetException e) {
      throw new UnsupportedOperationException("handle me gracefully", e);
    }

    //
    this.contextLifeCycle = contextLifeCycle;
    this.injectionContext = injectionContext;
  }

  public ApplicationContext getContext() {
    return contextLifeCycle.peek();
  }

  public void stop() {
    if (contextLifeCycle != null) {
      contextLifeCycle.release();
    }
    if (injectionContext != null) {
      injectionContext.shutdown();
    }
  }
}
