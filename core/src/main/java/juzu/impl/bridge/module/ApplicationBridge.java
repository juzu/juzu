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
package juzu.impl.bridge.module;

import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.bridge.BridgeContext;
import juzu.impl.plugin.application.Application;
import juzu.impl.asset.AssetServer;
import juzu.impl.common.Logger;
import juzu.impl.common.RunMode;
import juzu.impl.common.Tools;
import juzu.impl.inject.spi.Injector;
import juzu.impl.resource.ResourceResolver;
import juzu.impl.runtime.ApplicationRuntime;

/**
 * Bridge an application.
 *
 * @author Julien Viet
 */
public class ApplicationBridge extends Bridge {

  /** . */
  private ModuleContextImpl module;

  /** . */
  private ApplicationRuntime<?, ?> application;

  /** . */
  private final Injector injector;

  /** . */
  private RunMode runMode;

  public ApplicationBridge(
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

  public RunMode getRunMode() {
    if (runMode == null) {
      String runModeValue = context.getInitParameter("juzu.run_mode");
      if (runModeValue != null) {
        runMode = RunMode.parse(runModeValue);
        if (runMode == null) {
          log.log("Unparseable run mode " + runModeValue + " will use prod instead");
          runMode = RunMode.PROD;
        }
      } else {
        runMode = RunMode.PROD;
      }
    }
    return runMode;
  }

  public boolean refresh(boolean recompile) throws Exception {

    if (module == null) {

      //
      module = (ModuleContextImpl)context.getAttribute("juzu.module");
      if (module == null) {
        context.setAttribute("juzu.module", module = new ModuleContextImpl(log, this, context, resolver));
        module.runtime.refresh(true);
      }
      module.lease();
    }

    // For now refresh module first
    module.runtime.refresh(recompile);

    //
    if (application == null) {
      application = new ApplicationRuntime(
          log,
          module.runtime,
          injector,
          config.name,
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
