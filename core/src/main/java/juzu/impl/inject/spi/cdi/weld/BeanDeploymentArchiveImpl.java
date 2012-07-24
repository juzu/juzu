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

package juzu.impl.inject.spi.cdi.weld;

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
    List<URL> xmlURLs = new ArrayList<URL>();
    List<URL> fsURLs = new ArrayList<URL>();
    final ArrayList<String> beanClasses = new ArrayList<String>();
    for (final ReadFileSystem fileSystem : fileSystems) {
      fileSystem.traverse(new Visitor.Default() {
        @Override
        public void file(Object file, String name) throws IOException {
          if (name.endsWith(".class")) {
            StringBuilder fqn = new StringBuilder();
            fileSystem.packageOf(file, '.', fqn);
            if (fqn.length() > 0) {
              fqn.append('.');
            }
            fqn.append(name, 0, name.length() - ".class".length());
            beanClasses.add(fqn.toString());
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
