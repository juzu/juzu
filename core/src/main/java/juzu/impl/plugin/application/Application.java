/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
