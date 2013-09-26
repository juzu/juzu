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
package juzu.impl.inject.spi.cdi.provided;

import juzu.impl.plugin.application.Application;
import juzu.impl.common.Filter;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.inject.spi.Injector;
import juzu.impl.inject.spi.cdi.CDIContext;
import juzu.impl.inject.spi.cdi.CDIInjector;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.resource.ResourceResolver;

import javax.enterprise.inject.spi.BeanManager;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/** @author Julien Viet */
public class ProvidedCDIInjector extends CDIInjector {

  /** . */
  private static final Map<BeanManager, ProvidedCDIInjector> REGISTRY = Collections.synchronizedMap(new IdentityHashMap<BeanManager, ProvidedCDIInjector>());

  /** . */
  public static CDIInjector get(Object loader) {
    return REGISTRY.get(loader);
  }

  /** . */
  private final ClassLoader classLoader;

  /** . */
  private final BeanManager beanManager;

  /** . */
  private final ResourceResolver resolver;

  /** . */
  private final Application application;

  public ProvidedCDIInjector(
      ClassLoader classLoader,
      BeanManager beanManager,
      ApplicationDescriptor descriptor,
      ResourceResolver resolver) {

    // Register for later lookup
    REGISTRY.put(beanManager, this);

    Application application = new Application(this, descriptor, resolver);

    //
    this.classLoader = classLoader;
    this.beanManager = beanManager;
    this.resolver = resolver;
    this.application = application;
  }

  public Application getApplication() {
    return application;
  }

  @Override
  public boolean isProvided() {
    return true;
  }

  @Override
  public <P> Injector addFileSystem(ReadFileSystem<P> fs) {
    // Ignore
    return this;
  }

  @Override
  public Injector setClassLoader(ClassLoader classLoader) {
    // Ignore
    return this;
  }

  @Override
  public InjectionContext<?, ?> create(Filter<Class<?>> filter) throws Exception {
    return new CDIContext(this, filter) {
      @Override
      public ClassLoader getClassLoader() {
        return classLoader;
      }
      @Override
      protected BeanManager getBeanManager() {
        return beanManager;
      }
    };
  }

  @Override
  public Injector get() {
    return this;
  }
}
