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
    try {
      doStop();
    }
    catch (Exception e) {
      // Log it ?
    }
  }
}
