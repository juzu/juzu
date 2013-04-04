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
