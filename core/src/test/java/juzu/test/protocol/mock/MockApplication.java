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

package juzu.test.protocol.mock;

import juzu.impl.common.Name;
import juzu.impl.common.Tools;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.plugin.application.Application;
import juzu.impl.plugin.application.ApplicationException;
import juzu.impl.plugin.application.ApplicationLifeCycle;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.common.Logger;
import juzu.impl.plugin.module.ModuleLifeCycle;
import juzu.impl.resource.ResourceResolver;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockApplication<P> implements Closeable {

  /** . */
  final ClassLoader classLoader;

  /** . */
  private final ApplicationLifeCycle<P, ?> lifeCycle;

  public <L> MockApplication(
      ReadFileSystem<P> classes,
      ClassLoader classLoader,
      InjectorProvider implementation,
      Name name) throws Exception {

    /** . */
    Logger log = new Logger() {
      public void log(CharSequence msg) {
//        System.out.println("[" + name + "] " + msg);
        System.out.println("[" + "] " + msg);
      }

      public void log(CharSequence msg, Throwable t) {
//        System.err.println("[" + name + "] " + msg);
        System.err.println("[" + "] " + msg);
        t.printStackTrace(System.err);
      }
    };

    //
    ModuleLifeCycle<P, P> module = new ModuleLifeCycle.Static<P, P>(log, classLoader, classes);

    //
    ApplicationLifeCycle<P, P> lifeCycle = new ApplicationLifeCycle<P, P>(
        log,
        module,
        implementation,
        name,
        classes,
        null,
        new ResourceResolver() {
          public URL resolve(String uri) {
            return null;
          }
        });

    //
    this.classLoader = classLoader;
    this.lifeCycle = lifeCycle;
  }

  public MockApplication<P> init() throws Exception {
    lifeCycle.refresh();
    return this;
  }

  public ApplicationLifeCycle<P, ?> getLifeCycle() {
    return lifeCycle;
  }

  public Application getContext() {
    return lifeCycle.getApplication();
  }

  void invoke(RequestBridge bridge) throws ApplicationException {
    lifeCycle.getApplication().invoke(bridge);
  }

  public MockClient client() {
    return new MockClient(this);
  }

  public void close() throws IOException {
    Tools.safeClose(lifeCycle);
  }
}
