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
