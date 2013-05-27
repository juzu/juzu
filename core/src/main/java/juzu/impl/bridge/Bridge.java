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
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Bridge implements Closeable {

  /** . */
  public final Logger log;

  /** . */
  private final AssetServer server;

  /** . */
  private final BridgeConfig config;

  /** . */
  private final ReadFileSystem<?> resources;

  /** . */
  private final ResourceResolver<URL> resolver;

  /** . */
  public final Module module;

  /** . */
  public ApplicationLifeCycle<?, ?> application;

  public Bridge(Logger log, Module module, BridgeConfig config, ReadFileSystem<?> resources, AssetServer server, ResourceResolver<URL> resolver) {
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
