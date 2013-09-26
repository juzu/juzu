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
package juzu.impl.bridge.provided;

import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.bridge.BridgeContext;
import juzu.impl.common.Tools;
import juzu.impl.inject.spi.Injector;
import juzu.impl.plugin.application.Application;
import juzu.impl.asset.AssetServer;
import juzu.impl.common.Logger;
import juzu.impl.common.RunMode;
import juzu.impl.inject.spi.BeanLifeCycle;
import juzu.impl.inject.spi.cdi.provided.ProvidedCDIInjector;
import juzu.impl.resource.ResourceResolver;

import java.io.IOException;

/** @author Julien Viet */
public class ProvidedBridge extends Bridge {

  /** . */
  private Application application;

  /** . */
  private BeanLifeCycle applicationLifeCycle;

  /** . */
  private final Injector injector;

  public ProvidedBridge(
      BridgeContext context,
      Logger log,
      BridgeConfig config,
      AssetServer server,
      ResourceResolver resolver,
      Injector injector) {
    super(context, log, config, server, resolver);

    //
    this.injector = injector;
  }

  @Override
  public RunMode getRunMode() {
    return RunMode.DEV;
  }

  @Override
  public boolean refresh(boolean recompile) throws Exception {

    if (application == null) {

      // For now only works with CDI
      ProvidedCDIInjector injector = (ProvidedCDIInjector)this.injector;

      // Get App
      application = injector.getApplication();

      // Complete application start
      applicationLifeCycle = application.getInjectionContext().get(Application.class);

      // Complete application start
      applicationLifeCycle.get();

      //
      server.register(application);
    }
    return false;
  }

  @Override
  public Application getApplication() {
    return application;
  }

  public void close() throws IOException {
    Tools.safeClose(applicationLifeCycle);
    applicationLifeCycle = null;
  }
}
