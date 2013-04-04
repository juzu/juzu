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

package juzu.impl.plugin.module;

import juzu.impl.common.JSON;
import juzu.impl.common.Logger;
import juzu.impl.common.RunMode;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.war.WarFileSystem;

import java.io.File;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractWarModuleContext implements ModuleContext {

  /** . */
  private final Logger log;

  /** . */
  private WarFileSystem resourcePath;

  /** . */
  private ModuleLifeCycle<?> lifeCycle;

  /** . */
  private RunMode runMode;

  protected AbstractWarModuleContext(Logger log) {
    this.log = log;
  }

  public JSON getConfig() throws Exception {
    URL cfg = getClassLoader().getResource("juzu/config.json");
    String s = Tools.read(cfg);
    return (JSON)JSON.parse(s);
  }

  protected abstract WarFileSystem createFileSystem(String mountPoint);

  protected abstract String getInitParameter(String parameterName);

  public ReadFileSystem<?> getResourcePath() {
    if (resourcePath == null) {
      resourcePath = createFileSystem("/WEB-INF/");
    }
    return resourcePath;
  }

  public RunMode getRunMode() {
    if (runMode == null) {
      String runModeValue = getInitParameter("juzu.run_mode");
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

  public ModuleLifeCycle<?> getLifeCycle() {
    if (lifeCycle == null) {
      String srcPath = getInitParameter("juzu.src_path");
      ReadFileSystem<?> sourcePath = srcPath != null ? new DiskFileSystem(new File(srcPath)) : createFileSystem("/WEB-INF/src/");
      RunMode runMode = getRunMode();
      if (runMode.isDynamic()) {
        lifeCycle = new ModuleLifeCycle.Dynamic(log, Thread.currentThread().getContextClassLoader(), sourcePath);
      } else {
        lifeCycle = new ModuleLifeCycle.Static(log, Thread.currentThread().getContextClassLoader(), createFileSystem("/WEB-INF/classes/"));
      }
    }
    return lifeCycle;
  }
}
