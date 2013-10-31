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
package juzu.impl.bridge.spi.servlet;

import juzu.impl.bridge.BridgeContext;
import juzu.impl.common.JSON;
import juzu.impl.common.JUL;
import juzu.impl.common.Logger;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/** @author Julien Viet */
public abstract class AbstractBridgeContext implements BridgeContext {

  /** . */
  public static final String SOURCE_PATH = "juzu.src_path";

  /** . */
  ReadFileSystem<?> sourcePath;

  public final ReadFileSystem<?> getSourcePath() {
    if (sourcePath == null) {
      String sourcePathParam = getInitParameter(SOURCE_PATH);
      if (sourcePathParam != null) {
        sourcePath = new DiskFileSystem(new File(sourcePathParam));
      } else {
        try {
          URL configURL = getClassLoader().getResource("juzu/config.json");
          if (configURL != null) {
            String configValue = Tools.read(configURL);
            JSON config = (JSON)JSON.parse(configValue);
            String sourcePathValue = config.getString("sourcepath");
            if (sourcePathValue != null) {
              File sourcePathRoot = new File(sourcePathValue);
              if (sourcePathRoot.isDirectory() && sourcePathRoot.exists()) {
                sourcePath = new DiskFileSystem(sourcePathRoot);
              }
            }
          }
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return sourcePath;
  }

  public final Logger getLogger(String name) {
    return JUL.getLogger(name);
  }
}
