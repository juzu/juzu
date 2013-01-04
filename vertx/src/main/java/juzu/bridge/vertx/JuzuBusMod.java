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
package juzu.bridge.vertx;

import juzu.impl.common.Name;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.json.JsonObject;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JuzuBusMod extends BusModBase {


  /** . */
  private Application application;

  @Override
  public void start() {
    try {
      _start();
    }
    catch (Exception e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  public void _start() throws Exception {

    JsonObject config = container.getConfig();

    //
    String main = config.getString("main");
    if (main == null) {
      throw new Exception("No application main specified");
    }

    //
    Integer port = config.getInteger("port");
    if (port == null) {
      port = 8080;
    }

    //
    Name mainName = Name.parse(main);
    Name infoName = mainName.append("package-info");
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    URL infoURL = loader.getResource(infoName.toString().replace('.', '/') + ".java");
    if (infoURL == null) {
      throw new Exception("No application found at " + infoName);
    }

    // Build file system
    File root = new File(infoURL.toURI()).getParentFile();
    for (int i = 0;i < mainName.size();i++) {
      root = root.getParentFile();
    }
    DiskFileSystem sourcePath = new DiskFileSystem(root);

    //
    application = new Application(container, vertx, loader, sourcePath, mainName, port);
    application.start();
  }

  @Override
  public void stop() throws Exception {
    if (application != null) {
      application.stop();
    }
  }
}
