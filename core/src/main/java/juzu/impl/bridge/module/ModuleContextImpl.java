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

import juzu.impl.bridge.BridgeContext;
import juzu.impl.common.JSON;
import juzu.impl.common.Logger;
import juzu.impl.common.RunMode;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.plugin.module.ModuleContext;
import juzu.impl.resource.ResourceResolver;
import juzu.impl.runtime.ModuleRuntime;

import java.net.URL;

/**
 * The shared module context for a bridge.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ModuleContextImpl implements ModuleContext {

  /** . */
  final BridgeContext bridgeContext;

  /** . */
  final ResourceResolver resolver;

  /** . */
  final ModuleRuntime<?> runtime;

  /** . */
  final RunMode runMode;

  public ModuleContextImpl(Logger log, BridgeContext bridgeContext, ResourceResolver resolver) {

    //
    String runModeValue = bridgeContext.getInitParameter("juzu.run_mode");
    RunMode runMode;
    if (runModeValue != null) {
      runModeValue = Tools.interpolate(runModeValue, System.getProperties());
      runMode = RunMode.parse(runModeValue);
      if (runMode == null) {
        log.info("Unparseable run mode " + runModeValue + " will use prod instead");
        runMode = RunMode.PROD;
      }
    } else {
      runMode = RunMode.PROD;
    }

    //
    ModuleRuntime<?> lifeCycle;
    if (runMode.isDynamic()) {
      ReadFileSystem<?> sourcePath = bridgeContext.getSourcePath();
      log.info("Initializing live module at " + sourcePath.getDescription());
      lifeCycle = new ModuleRuntime.Dynamic(log, Thread.currentThread().getContextClassLoader(), sourcePath);
    } else {
      log.info("Initializing module in " + runMode.name().toLowerCase() + " mode");
      ReadFileSystem<?> classPath = bridgeContext.getClassPath();
      lifeCycle = new ModuleRuntime.Static(log, Thread.currentThread().getContextClassLoader(), classPath);
    }

    //
    this.bridgeContext = bridgeContext;
    this.resolver = resolver;
    this.runtime = lifeCycle;
    this.runMode = runMode;
  }

  public JSON getConfig() throws Exception {
    ClassLoader classLoader = getClassLoader();
    URL cfg = classLoader.getResource("juzu/config.json");
    String s = Tools.read(cfg);
    return (JSON)JSON.parse(s);
  }

  public ReadFileSystem<?> getResourcePath() {
    return bridgeContext.getResourcePath();
  }

  public RunMode getRunMode() {
    return runMode;
  }

  public ClassLoader getClassLoader() {
    return runtime.getClassLoader();
  }

  public ResourceResolver getServerResolver() {
    return resolver;
  }
}
