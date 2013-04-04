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

package juzu.impl.inject.spi.cdi;

import juzu.Scope;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Provider;
import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class DeclaredProviderBean extends AbstractDeclaredBean {

  /** . */
  private final Class<? extends Provider> providerType;

  /** . */
  protected AnnotatedType at;

  /** . */
  protected InjectionTarget it;

  /** . */
  protected AnnotatedType providerAT;

  /** . */
  protected InjectionTarget providerIT;

  DeclaredProviderBean(Class<?> type, Scope scope, Iterable<Annotation> qualifiers, Class<? extends Provider> providerType) {
    super(type, scope, qualifiers);

    //
    this.providerType = providerType;
  }

  @Override
  void register(BeanManager manager) {
    super.register(manager);

    //
    providerAT = manager.createAnnotatedType(providerType);
    providerIT = manager.createInjectionTarget(providerAT);
  }

  public Object create(CreationalContext ctx) {
    Object instance;
    try {
      Provider provider = (Provider)providerIT.produce(ctx);
      providerIT.inject(provider, ctx);
      providerIT.postConstruct(provider);

      // Get instance
      instance = provider.get();

      // Now get rid of provider
      providerIT.preDestroy(provider);
      providerIT.dispose(provider);
    }
    catch (Exception e) {
      throw new CreationException(e.getCause());
    }
    return instance;
  }

  public void destroy(Object instance, CreationalContext ctx) {
    ctx.release();
  }
}
