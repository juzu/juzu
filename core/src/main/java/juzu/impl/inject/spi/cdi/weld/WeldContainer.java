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

import juzu.Scope;
import juzu.impl.inject.ScopeController;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.inject.spi.cdi.Container;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class WeldContainer extends Container {

  /** . */
  final ClassLoader classLoader;

  /** . */
  final Bootstrap bootstrap;

  /** . */
  private BeanManager manager;

  @Override
  protected void doStart(List<ReadFileSystem<?>> fileSystems) throws Exception {
    final BeanDeploymentArchiveImpl bda = new BeanDeploymentArchiveImpl(this, "foo", fileSystems);

    //
    Deployment deployment = new Deployment() {

      /** . */
      final SimpleServiceRegistry registry = new SimpleServiceRegistry();

      /** . */
      final List<BeanDeploymentArchive> bdas = Arrays.<BeanDeploymentArchive>asList(bda);

      public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
        return bdas;
      }

      public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
        return bda;
      }

      public ServiceRegistry getServices() {
        return registry;
      }

      public Iterable<Metadata<Extension>> getExtensions() {
        return bootstrap.loadExtensions(Thread.currentThread().getContextClassLoader());
      }
    };

    //
    bootstrap.startContainer(Environments.SERVLET, deployment);
    bootstrap.startInitialization();
    bootstrap.deployBeans();
    bootstrap.validateBeans();
    bootstrap.endInitialization();

    //
    manager = bootstrap.getManager(bda);
  }

  public WeldContainer(ClassLoader classLoader, ScopeController scopeController, Set<Scope> scopes) {
    super(scopeController, scopes);

    //
    this.classLoader = classLoader;
    this.bootstrap = new WeldBootstrap();
  }

  @Override
  public BeanManager getManager() {
    return manager;
  }

  @Override
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  @Override
  protected void doStop() {
    if (bootstrap != null) {
      bootstrap.shutdown();
    }
  }
}
