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

import juzu.impl.common.Logger;
import juzu.impl.common.RunMode;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.resource.ResourceResolver;

/** @author Julien Viet */
public abstract class BridgeContext {

  /** . */
  private RunMode runMode;

  public RunMode getRunMode() {
    if (runMode == null) {
      String runModeValue = getInitParameter("juzu.run_mode");
      if (runModeValue != null) {
        runModeValue = Tools.interpolate(runModeValue, System.getProperties());
        runMode = RunMode.parse(runModeValue);
        if (runMode == null) {
          // log.info("Unparseable run mode " + runModeValue + " will use prod instead");
          runMode = RunMode.PROD;
        }
      } else {
        runMode = RunMode.PROD;
      }
    }
    return runMode;
  }

  public abstract Logger getLogger(String name);

  public abstract ReadFileSystem<?> getClassPath();

  public abstract ReadFileSystem<?> getSourcePath();

  public abstract ReadFileSystem<?> getResourcePath();

  public abstract ClassLoader getClassLoader();

  public abstract String getInitParameter(String name);

  public abstract ResourceResolver getResolver();

  public abstract Object getAttribute(String key);

  public abstract void setAttribute(String key, Object value);

}
