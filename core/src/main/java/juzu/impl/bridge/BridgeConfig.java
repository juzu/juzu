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

package juzu.impl.bridge;

import juzu.impl.common.QN;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.common.Tools;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BridgeConfig {

  /** . */
  public static final int STATIC_MODE = 0;

  /** . */
  public static final int DYNAMIC_MODE = 1;

  /** . */
  public static final String RUN_MODE = "juzu.run_mode";

  /** . */
  public static final String INJECT = "juzu.inject";

  /** . */
  public static final String APP_NAME = "juzu.app_name";

  /** . */
  public static final String SOURCE_PATH = "juzu.src_path";

  /** . */
  public static final Set<String> NAMES = Collections.unmodifiableSet(Tools.set(RUN_MODE, INJECT, APP_NAME));

  /** . */
  public final int runMode;

  /** . */
  public final QN name;

  /** . */
  public final InjectorProvider injectImpl;

  public BridgeConfig(int runMode, QN name, InjectorProvider injectImpl) {
    this.runMode = runMode;
    this.name = name;
    this.injectImpl = injectImpl;
  }

  public static QN getApplicationName(Map<String, String> config) {
    String applicationName = config.get("juzu.app_name");
    return applicationName != null ? QN.parse(applicationName) : null;
  }

  public static int getRunMode(Map<String, String> config) {
    String runMode = config.get("juzu.run_mode");
    runMode = runMode == null ? "prod" : runMode.trim().toLowerCase();
    int mode;
    if ("dev".equals(runMode)) {
      mode = DYNAMIC_MODE;
    } else {
      mode = STATIC_MODE;
    }
    return mode;
  }

  public static InjectorProvider getInjectImplementation(Map<String, String> config) throws Exception {
    String inject = config.get("juzu.inject");
    InjectorProvider implementation;
    if (inject == null) {
      implementation = InjectorProvider.CDI_WELD;
    } else {
      inject = inject.trim().toLowerCase();
      if ("weld".equals(inject)) {
        implementation = InjectorProvider.CDI_WELD;
      } else if ("spring".equals(inject)) {
        implementation = InjectorProvider.INJECT_SPRING;
      } else {
        throw new Exception("unrecognized inject vendor " + inject);
      }
    }
    return implementation;
  }

  public BridgeConfig(Map<String, String> config) throws Exception {
    this.name = getApplicationName(config);
    this.runMode = getRunMode(config);
    this.injectImpl = getInjectImplementation(config);
  }

  public boolean isProd() {
    return runMode == STATIC_MODE;
  }
}
