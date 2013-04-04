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
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class DeclaredBean extends AbstractDeclaredBean {

  /** . */
  protected InjectionTarget it;

  /** . */
  protected AnnotatedType at;

  DeclaredBean(Class<?> type, Scope scope, Iterable<Annotation> qualifiers) {
    super(type, scope, qualifiers);
  }

  void register(BeanManager manager) {
    super.register(manager);

    //
    AnnotatedType at = manager.createAnnotatedType(type);
    InjectionTarget it = manager.createInjectionTarget(at);

    //
    this.it = it;
    this.at = at;
  }

  public Object create(CreationalContext ctx) {
    Object instance = it.produce(ctx);
    it.inject(instance, ctx);
    it.postConstruct(instance);
    return instance;
  }


  public void destroy(Object instance, CreationalContext ctx) {
    it.preDestroy(instance);
    it.dispose(instance);
    ctx.release();
  }
}
