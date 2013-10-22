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
import juzu.impl.common.RunMode;
import juzu.impl.asset.AssetServer;
import juzu.impl.resource.ResourceResolver;

import java.io.Closeable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class Bridge implements Closeable {

  /** . */
  public final BridgeContext context;

  /** . */
  protected final AssetServer server;

  /** . */
  protected final BridgeConfig config;

  /** . */
  protected final ResourceResolver resolver;

  public Bridge(
      BridgeContext context,
      BridgeConfig config,
      AssetServer server,
      ResourceResolver resolver) {

    //
    this.context = context;
    this.config = config;
    this.server = server;
    this.resolver = resolver;
  }

  public BridgeConfig getConfig() {
    return config;
  }

  public boolean refresh() throws Exception {
    return refresh(true);
  }

  public abstract RunMode getRunMode();

  public abstract boolean refresh(boolean recompile) throws Exception;

  public abstract Application getApplication();

}
