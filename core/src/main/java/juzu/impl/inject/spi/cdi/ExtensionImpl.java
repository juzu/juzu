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
import juzu.impl.plugin.application.Application;
import juzu.impl.common.JSON;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.common.Tools;
import juzu.impl.inject.spi.cdi.provided.ProvidedCDIInjector;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.resource.ResourceResolver;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.inject.Singleton;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Juzu CDI extension.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ExtensionImpl implements Extension {

  /** . */
  private CDIContext context;

  /** The singletons to shut down. */
  private final List<Bean<?>> singletons;

  /** . */
  private boolean configured;

  public ExtensionImpl() {
    this.context = CDIContext.boot.get();
    this.singletons = new ArrayList<Bean<?>>();
    this.configured = false;
  }

  void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event, final BeanManager beanManager) {

    // We are in provided mode
    if (this.context == null) {

      // This is not really clean : it couples the inject to the application notion
      // but avoiding that would mean to introduce more complex stuff and so
      // it's fine for the moment

      try {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream in = cl.getResourceAsStream("juzu/config.json");
        String serializedConfig = Tools.read(in);
        JSON config = (JSON)JSON.parse(serializedConfig);
        JSON applications = config.getJSON("application");
        if (applications.names().size() != 1) {
          throw new RuntimeException("Was expecting application size to be 1 instead of " + applications);
        }
        String packageFQN = applications.names().iterator().next();
        ApplicationDescriptor descriptor = ApplicationDescriptor.create(cl, packageFQN);
        // For now we don't resolve anything...
        ResourceResolver resourceResolver = new ResourceResolver() {
          public URL resolve(String uri) {
            return null;
          }
        };

        //
        ProvidedCDIInjector injector = new ProvidedCDIInjector(cl, beanManager, descriptor, resourceResolver);

        // We start the application
        // it should:
        // - instantiate the plugins
        // - bind the beans from the plugins in the container
        // we rely on the lazy nature of the beans for not really starting...
        Application application = injector.getApplication();
        application.start();

        // At this point the application is not really started
        // we must go through the other CDI phases for effectively registering
        // the beans in the container
        this.context = (CDIContext)application.getInjectionContext();
      }
      catch (Exception e) {
        throw new UnsupportedOperationException(e);
      }
    }
  }

  <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {

    AnnotatedType<T> annotatedType = pat.getAnnotatedType();
    Class<T> type = annotatedType.getJavaClass();

    // Determine if bean should be processed
    boolean veto = !context.filter.accept(type);
    if (!veto) {
      for (AbstractBean boundBean : context.injector.boundBeans) {
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

  void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
    for (Scope scope : context.injector.scopes) {
      if (!scope.isBuiltIn()) {
        event.addContext(new ContextImpl(context.injector.scopeController, scope, scope.getAnnotationType()));
      }
    }

    // Add the manager
    event.addBean(new SingletonBean(InjectionContext.class, Tools.set(AbstractBean.DEFAULT_QUALIFIER, AbstractBean.ANY_QUALIFIER), context));

    // Add bound beans
    for (AbstractBean bean : context.injector.boundBeans) {
      bean.register(beanManager);
      event.addBean(bean);
    }
  }

  void processBean(@Observes ProcessBean event, BeanManager beanManager) {
    Bean bean = event.getBean();
    context.beans.add(bean);

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