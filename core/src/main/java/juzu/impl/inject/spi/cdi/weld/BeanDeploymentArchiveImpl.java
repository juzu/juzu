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

import juzu.impl.common.Tools;
import juzu.impl.fs.Visitor;
import juzu.impl.fs.spi.ReadFileSystem;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.resources.ClassLoaderResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class BeanDeploymentArchiveImpl implements BeanDeploymentArchive {

  /** . */
  private final WeldContainer owner;

  /** . */
  private final String id;

  /** . */
  private final Collection<String> beanClasses;

  /** . */
  private final BeansXml xml;

  /** . */
  private final ServiceRegistry registry;

  BeanDeploymentArchiveImpl(
    WeldContainer owner,
    String id,
    List<ReadFileSystem<?>> fileSystems) throws IOException {

    // A bit unchecked but well it's ok here
    ArrayList<URL> xmlURLs = new ArrayList<URL>();
    final StringBuilder buffer = new StringBuilder();
    final ArrayList<String> beanClasses = new ArrayList<String>();
    for (final ReadFileSystem fileSystem : fileSystems) {
      fileSystem.traverse(new Visitor.Default() {
        @Override
        public void enterDir(Object dir, String name) throws IOException {
          if (name.length() > 0) {
            buffer.append(name).append('.');
          }
        }
        @Override
        public void file(Object file, String name) throws IOException {
          if (name.endsWith(".class")) {
            int len = name.length() - ".class".length();
            buffer.append(name, 0, len);
            String fqn = buffer.toString();
            buffer.setLength(buffer.length() - len);
            if (!fqn.startsWith("juzu.impl.inject.spi.guice.") && !fqn.startsWith("juzu.impl.inject.spi.spring.")) {
              beanClasses.add(fqn);
            }
          }
        }
        @Override
        public void leaveDir(Object dir, String name) throws IOException {
          if (name.length() > 0) {
            buffer.setLength(buffer.length() - name.length() - 1);
          }
        }
      });

      //
      // fsURLs.add(fileSystem.getURL());

      //
      Object beansPath = fileSystem.getPath(Arrays.asList("META-INF", "beans.xml"));
      if (beansPath != null) {
        xmlURLs.add(fileSystem.getURL(beansPath));
      }
    }

    //
    BeansXml xml = owner.bootstrap.parse(xmlURLs);

    //
//      URLClassLoader classLoader = new URLClassLoader(fsURLs.toArray(new URL[fsURLs.size()]), owner.classLoader);
    ResourceLoader loader = new ClassLoaderResourceLoader(owner.classLoader);

    //
    ServiceRegistry registry = new SimpleServiceRegistry();
    registry.add(ResourceLoader.class, loader);

    //
    this.beanClasses = beanClasses;
    this.xml = xml;
    this.id = id;
    this.registry = registry;
    this.owner = owner;
  }

  public ClassLoader getClassLoader() {
    return owner.classLoader;
  }

  public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
    return Collections.emptyList();
  }

  public Collection<String> getBeanClasses() {
    return beanClasses;
  }

  public BeansXml getBeansXml() {
    return xml;
  }

  public Collection<EjbDescriptor<?>> getEjbs() {
    return Collections.emptyList();
  }

  public ServiceRegistry getServices() {
    return registry;
  }

  public String getId() {
    return id;
  }
}
