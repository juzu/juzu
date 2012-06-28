/*
 * Copyright (C) 2011 eXo Platform SAS.
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

import juzu.impl.application.ApplicationBootstrap;
import juzu.impl.application.ApplicationContext;
import juzu.impl.application.ApplicationException;
import juzu.impl.application.ApplicationRuntime;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.jar.JarFileSystem;
import juzu.impl.inject.spi.InjectImplementation;
import juzu.impl.request.spi.RequestBridge;
import juzu.impl.utils.JSON;
import juzu.impl.utils.Logger;
import juzu.impl.utils.Tools;
import juzu.test.AbstractTestCase;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.jar.JarFile;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockApplication<P> {

  /** . */
  final ClassLoader classLoader;

  /** . */
  private final ApplicationRuntime<P, ?, ?> runtime;

  public <L> MockApplication(ReadFileSystem<P> classes, ClassLoader classLoader, InjectImplementation implementation) throws Exception {
    P f = classes.getPath(Arrays.asList("juzu", "config.json"));
    if (f == null) {
      throw new Exception("Cannot find config properties");
    }

    //
    URL url = classes.getURL(f);
    String s = Tools.read(url);
    JSON props = (JSON)JSON.parse(s);

    //
    if (props.names().size() != 1) {
      throw AbstractTestCase.failure("Could not find an application to start " + props);
    }
    String name = props.names().iterator().next();

    //
    URL location = ApplicationBootstrap.class.getProtectionDomain().getCodeSource().getLocation();
    String protocol = location.getProtocol();
    ReadFileSystem<L> libs;
    if ("file".equals(protocol)) {
      File file = new File(location.toURI());
      if (file.isDirectory()) {
        libs = (ReadFileSystem<L>)new DiskFileSystem(file);
      } else if (file.isFile() && file.getName().endsWith(".jar")) {
        libs = (ReadFileSystem<L>)new JarFileSystem(new JarFile(file));
      } else {
        throw AbstractTestCase.failure("Could not create file system from location " + location);
      }
    } else {
      throw AbstractTestCase.failure("Could not create file system from location " + location);
    }

    /** . */
    Logger log = new Logger() {
      public void log(CharSequence msg) {
        System.out.print(msg);
      }

      public void log(CharSequence msg, Throwable t) {
        System.err.println(msg);
        t.printStackTrace(System.err);
      }
    };

    //
    ApplicationRuntime.Static<P, P, L> runtime = new ApplicationRuntime.Static<P, P, L>(log);
    runtime.setClasses(classes);
    runtime.setResources(classes);
    runtime.setLibs(libs);
    runtime.setClassLoader(classLoader);
    runtime.setName(name);
    runtime.setInjectImplementation(implementation);

    //
    this.classLoader = classLoader;
    this.runtime = runtime;
  }

  public MockApplication<P> init() throws Exception {
    runtime.boot();
    return this;
  }

  public ApplicationRuntime<P, ?, ?> getRuntime() {
    return runtime;
  }

  public ApplicationContext getContext() {
    return runtime.getContext();
  }

  void invoke(RequestBridge bridge) throws ApplicationException {
    runtime.getContext().invoke(bridge);
  }

  public MockClient client() {
    return new MockClient(this);
  }
}
