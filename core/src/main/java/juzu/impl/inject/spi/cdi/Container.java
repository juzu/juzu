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
import juzu.impl.inject.ScopeController;
import juzu.impl.fs.spi.ReadFileSystem;

import javax.enterprise.inject.spi.BeanManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class Container {

  /** . */
  static final ThreadLocal<Container> boot = new ThreadLocal<Container>();

  /** . */
  private List<ReadFileSystem<?>> fileSystems;

  /** . */
  final Set<Scope> scopes;

  /** . */
  final ScopeController scopeController;

  protected Container(ScopeController scopeController, Set<Scope> scopes) {
    this.fileSystems = new ArrayList<ReadFileSystem<?>>();
    this.scopes = scopes;
    this.scopeController = scopeController;
  }

  public abstract BeanManager getManager();

  public abstract ClassLoader getClassLoader();

  public void addFileSystem(ReadFileSystem<?> fileSystem) {
    fileSystems.add(fileSystem);
  }

  protected abstract void doStart(List<ReadFileSystem<?>> fileSystems) throws Exception;

  protected abstract void doStop();

  public void start() throws Exception {
    boot.set(this);
    try {
      doStart(fileSystems);
    }
    finally {
      boot.set(null);
    }
  }

  public void stop() {
    doStop();
  }
}
