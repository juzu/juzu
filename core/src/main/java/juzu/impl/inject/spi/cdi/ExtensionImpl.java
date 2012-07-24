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
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.common.Tools;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Juzu CDI extension.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ExtensionImpl implements Extension {

  /** . */
  private final static AnnotationLiteral<Produces> PRODUCES_ANNOTATION_LITERAL = new AnnotationLiteral<Produces>() {
  };

  /** . */
  private final CDIContext manager;

  /** The singletons to shut down. */
  private List<Bean<?>> singletons;

  public ExtensionImpl() {
    this.manager = CDIContext.boot.get();
    this.singletons = new ArrayList<Bean<?>>();
  }

  <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {

    AnnotatedType<T> annotatedType = pat.getAnnotatedType();
    Class<T> type = annotatedType.getJavaClass();

    // Determine if bean is a singleton bound
    boolean bound = false;
    for (AbstractBean boundBean : manager.boundBeans) {
      if (boundBean.getBeanClass().isAssignableFrom(type)) {
        bound = true;
      }
    }

    //
    boolean veto = bound || manager.filter != null && !manager.filter.acceptBean(type);

    //
    if (veto) {
      pat.veto();
    }
  }

  void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
    Container container = Container.boot.get();

    //
    for (Scope scope : container.scopes) {
      if (!scope.isBuiltIn()) {
        event.addContext(new ContextImpl(container.scopeController, scope, scope.getAnnotationType()));
      }
    }

    // Add the manager
    event.addBean(new SingletonBean(InjectionContext.class, Tools.set(AbstractBean.DEFAULT_QUALIFIER, AbstractBean.ANY_QUALIFIER), manager));

    // Add bound beans
    for (AbstractBean bean : manager.boundBeans) {
      bean.register(beanManager);
      event.addBean(bean);
    }
  }

  void processBean(@Observes ProcessBean event, BeanManager beanManager) {
    Bean bean = event.getBean();
    manager.beans.add(bean);

    //
    if (bean.getScope() == Singleton.class) {
      singletons.add(bean);
    }
  }

  public void beforeShutdown(@Observes BeforeShutdown event, BeanManager beanManager) {
    // Take care of destroying singletons
    for (Bean singleton : singletons) {
      CreationalContext cc = beanManager.createCreationalContext(singleton);
      Object o = beanManager.getReference(singleton, singleton.getBeanClass(), cc);
      singleton.destroy(o, cc);
    }
  }
}