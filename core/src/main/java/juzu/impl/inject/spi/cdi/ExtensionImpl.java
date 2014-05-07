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
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.common.Tools;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
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
  private final CDIContext manager;

  /** The singletons to shut down. */
  private List<Bean<?>> singletons;

  public ExtensionImpl() {
    this.manager = CDIContext.boot.get();
    this.singletons = new ArrayList<Bean<?>>();
  }

  <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
    if (manager != null) {
      AnnotatedType<T> annotatedType = pat.getAnnotatedType();
      Class<T> type = annotatedType.getJavaClass();

      // Determine if bean should be processed
      boolean veto = !manager.filter.accept(type);
      if (!veto) {
        for (AbstractBean boundBean : manager.boundBeans) {
          Class<?> beanType = boundBean.getBeanClass();
          if (beanType.isAssignableFrom(type)) {
            veto = true;
            break;
          }
        }
      }

      //
      if (veto) {
        pat.veto();
      }
    }
  }

  void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
    if (manager != null) {
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
  }

  void processBean(@Observes ProcessBean event, BeanManager beanManager) {
    if (manager != null) {
      Bean bean = event.getBean();
      manager.beans.add(bean);

      //
      if (bean.getScope() == Singleton.class) {
        singletons.add(bean);
      }
    }
  }

  public void beforeShutdown(@Observes BeforeShutdown event, BeanManager beanManager) {
    if (manager != null) {
      // Take care of destroying singletons
      for (Bean singleton : singletons) {
        CreationalContext cc = beanManager.createCreationalContext(singleton);
        Object o = beanManager.getReference(singleton, singleton.getBeanClass(), cc);
        singleton.destroy(o, cc);
      }
    }
  }
}