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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The shared module context for a bridge.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class ModuleContextImpl implements ModuleContext {

  /** . */
  final AtomicInteger leases;

  /** . */
  final BridgeContext bridgeContext;

  /** . */
  final ResourceResolver resolver;

  /** . */
  final ApplicationBridge bridge;

  /** . */
  final ModuleRuntime<?> runtime;

  /** . */
  final RunMode runMode;

  protected ModuleContextImpl(Logger log, ApplicationBridge bridge, BridgeContext bridgeContext, ResourceResolver resolver) {

    //
    ModuleRuntime<?> lifeCycle;
    RunMode runMode = bridge.getRunMode();
    if (runMode.isDynamic()) {
      ReadFileSystem<?> sourcePath = bridgeContext.getSourcePath();
      lifeCycle = new ModuleRuntime.Dynamic(log, Thread.currentThread().getContextClassLoader(), sourcePath);
    } else {
      ReadFileSystem<?> classPath = bridgeContext.getClassPath();
      lifeCycle = new ModuleRuntime.Static(log, Thread.currentThread().getContextClassLoader(), classPath);
    }

    //
    this.bridgeContext = bridgeContext;
    this.resolver = resolver;
    this.bridge = bridge;
    this.runtime = lifeCycle;
    this.leases = new AtomicInteger();
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

  public synchronized void lease() {
    leases.incrementAndGet();
  }

  /**
   * Return true if the module has no references pointing to it.
   *
   * @return true when the module is not referenced anymore
   */
  public synchronized boolean release() {
    return leases.decrementAndGet() == 0;
  }
}
