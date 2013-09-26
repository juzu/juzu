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

package inject;

import juzu.Scope;
import juzu.impl.common.Filter;
import juzu.impl.inject.Scoped;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.inject.spi.Injector;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.inject.spi.InjectionContext;

import java.io.File;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractInjectTestCase<B, I> extends juzu.test.AbstractInjectTestCase {

  /** . */
  protected Injector bootstrap;

  /** . */
  protected InjectionContext<B, I> mgr;

  /** . */
  protected ReadFileSystem<?> fs;

  /** . */
  protected ScopingContextImpl scopingContext;

  public AbstractInjectTestCase(InjectorProvider di) {
    super(di);
  }

  protected final void init() throws Exception {
    init(getClass().getPackage().getName());
  }

  protected final void init(String pkg) throws Exception {
    File root = new File(AbstractInjectTestCase.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    assertTrue(root.exists());
    assertTrue(root.isDirectory());
    init(new DiskFileSystem(root, pkg), Thread.currentThread().getContextClassLoader());
  }

  protected final void init(ReadFileSystem<?> fs, ClassLoader classLoader) throws Exception {
    Injector bootstrap = getManager();
    bootstrap.addFileSystem(fs);
    bootstrap.setClassLoader(classLoader);

    //
    this.bootstrap = bootstrap;
    this.fs = fs;
  }

  protected final void boot(Scope... scopes) throws Exception {
    boot((Filter<Class<?>>)null, scopes);
  }

  protected final void boot(Filter<Class<?>> filter, Scope... scopes) throws Exception {
    mgr = boot(bootstrap, filter, scopes);
  }

  protected static <B, I> InjectionContext<B, I> boot(Injector injector, Scope... scopes) throws Exception {
    return boot(injector, null, scopes);
  }

  protected static <B, I> InjectionContext<B, I> boot(Injector injector, Filter<Class<?>> filter, Scope... scopes) throws Exception {
    for (Scope scope : scopes) {
      injector.addScope(scope);
    }
    if (filter == null) {
      return (InjectionContext<B, I>)injector.create();
    } else {
      return (InjectionContext<B, I>)injector.create(filter);
    }
  }

  protected final <T> T getBean(Class<T> beanType) throws Exception {
    return getBean(mgr, beanType);
  }

  protected static <B, I, T> T getBean(InjectionContext<B, I> context, Class<T> beanType) throws Exception {
    B bean = context.resolveBean(beanType);
    assertNotNull("Could not resolve bean of type " + beanType, bean);
    I beanInstance = context.create(bean);
    assertNotNull("Could not create bean instance of type " + beanType + " from bean " + bean, beanInstance);
    Object o = context.get(bean, beanInstance);
    assertNotNull("Could not obtain bean object from bean instance " + beanInstance + " of type " + beanType, o);
    return beanType.cast(o);
  }

  protected final Object getBean(String beanName) throws Exception {
    B bean = mgr.resolveBean(beanName);
    assertNotNull("Could not find bean " + beanName, bean);
    I beanInstance = mgr.create(bean);
    assertNotNull(beanInstance);
    return mgr.get(bean, beanInstance);
  }

  protected final void beginScoping() throws Exception {
    if (scopingContext != null) {
      throw failure("Already scoping");
    }
    mgr.getScopeController().begin(scopingContext = new ScopingContextImpl());
  }

  protected final void endScoping() throws Exception {
    if (scopingContext == null) {
      throw failure("Not scoping");
    }
    mgr.getScopeController().end();
    for (Scoped scoped : scopingContext.getEntries().values()) {
      scoped.destroy();
    }
    scopingContext = null;
  }

  protected final Injector getManager() throws Exception {
    return getDI().get();
  }
}
