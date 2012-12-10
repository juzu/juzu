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

import juzu.Application;
import juzu.Scope;
import juzu.impl.common.Filter;
import juzu.impl.common.Tools;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.inject.spi.BeanLifeCycle;
import juzu.impl.inject.spi.Injector;
import juzu.impl.plugin.Plugin;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class ApplicationBootstrap {

  /** . */
  public final Injector injector;

  /** . */
  public final ApplicationDescriptor descriptor;

  /** . */
  private BeanLifeCycle<ApplicationContext> contextLifeCycle;

  /** . */
  private InjectionContext<?, ?> injectionContext;

  ApplicationBootstrap(Injector injector, ApplicationDescriptor descriptor) {
    this.injector = injector;
    this.descriptor = descriptor;
  }

  void start() throws ApplicationException {
    _start();
  }

  private <B, I> void _start() throws ApplicationException {
    // Bind the application descriptor
    injector.bindBean(ApplicationDescriptor.class, null, descriptor);

    // Bind the application context
    injector.declareBean(ApplicationContext.class, null, null, null);

    // Bind the scopes
    for (Scope scope : Scope.values()) {
      injector.addScope(scope);
    }

    // Bind the plugins
    for (Plugin plugin : descriptor.getPlugins().values()) {
      Class aClass = plugin.getClass();
      Object o = plugin;
      injector.bindBean(aClass, null, o);
    }

    // Bind the beans
    for (BeanDescriptor bean : descriptor.getBeans()) {
      bean.bind(injector);
    }

    // Filter the classes:
    // any class beginning with juzu. is refused
    // any class prefixed with the application package is accepted
    // any other application class is refused (i.e a class having an ancestor package annotated with @Application)
    Filter<Class<?>> filter = new Filter<Class<?>>() {
      HashSet<String> blackList = new HashSet<String>();
      public boolean accept(Class<?> elt) {
        if (elt.getName().startsWith("juzu.")) {
          return false;
        } else if (elt.getPackage().getName().startsWith(descriptor.getPackageName())) {
          return true;
        } else {
          for (String currentPkg = elt.getPackage().getName();currentPkg != null;currentPkg = Tools.parentPackageOf(currentPkg)) {
            if (blackList.contains(currentPkg)) {
              return false;
            } else {
              try {
                Class<?> packageClass = descriptor.getApplicationLoader().loadClass(currentPkg + ".package-info");
                Application ann = packageClass.getAnnotation(Application.class);
                if (ann != null) {
                  blackList.add(currentPkg);
                  return false;
                }
              }
              catch (ClassNotFoundException e) {
                // Skip it
              }
            }
          }
          return true;
        }
      }
    };

    //
    InjectionContext<B, I> injectionContext;
    try {
      injectionContext = (InjectionContext<B, I>)injector.create(filter);
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

  ApplicationContext getContext() {
    return contextLifeCycle.peek();
  }

  void stop() {
    if (contextLifeCycle != null) {
      contextLifeCycle.release();
    }
    if (injectionContext != null) {
      injectionContext.shutdown();
    }
  }
}
