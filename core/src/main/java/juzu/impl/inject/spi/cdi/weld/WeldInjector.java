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
package juzu.impl.inject.spi.cdi.weld;

import juzu.impl.common.Filter;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.inject.spi.Injector;
import juzu.impl.inject.spi.cdi.CDIContext;
import juzu.impl.inject.spi.cdi.CDIInjector;
import juzu.impl.inject.spi.cdi.Container;

import javax.enterprise.inject.spi.BeanManager;
import java.util.ArrayList;
import java.util.List;

/** @author Julien Viet */
public class WeldInjector extends CDIInjector {

  /** . */
  private ClassLoader classLoader;

  /** . */
  private List<ReadFileSystem<?>> fileSystems;

  public WeldInjector() {
    this.classLoader = null;
    this.fileSystems = new ArrayList<ReadFileSystem<?>>();
  }

  public WeldInjector(WeldInjector that) {
    super(that);

    //
    this.classLoader = that.classLoader;
    this.fileSystems = new ArrayList<ReadFileSystem<?>>(that.fileSystems);
  }

  @Override
  public boolean isProvided() {
    return false;
  }

  @Override
  public <P> Injector addFileSystem(ReadFileSystem<P> fs) {
    fileSystems.add(fs);
    return this;
  }

  @Override
  public Injector setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  @Override
  public InjectionContext<?, ?> create(Filter<Class<?>> filter) throws Exception {
    final Container container = new WeldContainer(classLoader, scopeController, scopes);
    for (ReadFileSystem<?> fs : fileSystems) {
      container.addFileSystem(fs);
    }
    CDIContext cdiContext = new CDIContext(this, filter) {
      @Override
      public ClassLoader getClassLoader() {
        return container.getClassLoader();
      }
      @Override
      protected BeanManager getBeanManager() {
        return container.getManager();
      }
      @Override
      public void close() {
        container.stop();
      }
    };

    // Do the real boot
    CDIContext.boot.set(cdiContext);
    try {
      container.start();
    }
    finally {
      CDIContext.boot.set(cdiContext);
    }

    //
    return cdiContext;
  }

  @Override
  public Injector get() {
    return new WeldInjector(this);
  }
}
