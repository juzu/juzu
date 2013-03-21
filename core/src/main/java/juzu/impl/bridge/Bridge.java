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

package juzu.impl.bridge;

import juzu.impl.plugin.application.Application;
import juzu.impl.plugin.application.ApplicationLifeCycle;
import juzu.impl.asset.AssetServer;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.common.Logger;
import juzu.impl.common.Tools;
import juzu.impl.plugin.module.Module;
import juzu.impl.resource.ResourceResolver;

import java.io.Closeable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Bridge implements Closeable {

  /** . */
  private final Logger log;

  /** . */
  private final AssetServer server;

  /** . */
  private final BridgeConfig config;

  /** . */
  private final ReadFileSystem<?> resources;

  /** . */
  private final ResourceResolver resolver;

  /** . */
  public final Module module;

  /** . */
  public ApplicationLifeCycle<?, ?> application;

  public Bridge(Logger log, Module module, BridgeConfig config, ReadFileSystem<?> resources, AssetServer server, ResourceResolver resolver) {
    this.log = log;
    this.module = module;
    this.config = config;
    this.resources = resources;
    this.server = server;
    this.resolver = resolver;
  }

  public BridgeConfig getConfig() {
    return config;
  }

  public boolean refresh() throws Exception {
    return refresh(true);
  }

  public boolean refresh(boolean recompile) throws Exception {

    // For now refresh module first
    module.context.getLifeCycle().refresh(recompile);

    //
    if (application == null) {
      application = new ApplicationLifeCycle(
          log,
          module.context.getLifeCycle(),
          config.injectImpl,
          config.name,
          resources,
          server,
          resolver);
    }

    //
    return application.refresh();
  }

  public Application getApplication() {
    return application.getApplication();
  }

  public void close() {
    Tools.safeClose(application);
  }
}
