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
import juzu.impl.inject.spi.InjectImplementation;
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
  public static final int PROVIDED_MODE = 2;

  /** . */
  public static final String RUN_MODE = "juzu.run_mode";

  /** . */
  public static final String INJECT = "juzu.inject";

  /** . */
  public static final String APP_NAME = "juzu.app_name";

  /** . */
  public static final Set<String> NAMES = Collections.unmodifiableSet(Tools.set(RUN_MODE, INJECT, APP_NAME));

  /** . */
  public final int mode;

  /** . */
  public final QN name;

  /** . */
  public final InjectImplementation injectImpl;

  public BridgeConfig(int mode, QN name, InjectImplementation injectImpl) {
    this.mode = mode;
    this.name = name;
    this.injectImpl = injectImpl;
  }

  public BridgeConfig(Map<String, String> config) throws Exception {

    //
    String runMode = config.get("juzu.run_mode");
    runMode = runMode == null ? "prod" : runMode.trim().toLowerCase();

    //
    String inject = config.get("juzu.inject");
    InjectImplementation injectImpl;
    if (inject == null) {
      injectImpl = InjectImplementation.CDI_WELD;
    }
    else {
      inject = inject.trim().toLowerCase();
      if ("weld".equals(inject)) {
        injectImpl = InjectImplementation.CDI_WELD;
      }
      else if ("spring".equals(inject)) {
        injectImpl = InjectImplementation.INJECT_SPRING;
      }
      else {
        throw new Exception("unrecognized inject vendor " + inject);
      }
    }

    //
    int mode;
    if ("dev".equals(runMode)) {
      mode = DYNAMIC_MODE;
    } else if ("provided".equals(runMode)) {
      mode = PROVIDED_MODE;
    } else {
      mode = STATIC_MODE;
    }

    //
    String appName = config.get("juzu.app_name");

    //
    this.name = appName != null ? QN.parse(appName) : null;
    this.mode = mode;
    this.injectImpl = injectImpl;
  }

  public boolean isProd() {
    return mode == STATIC_MODE;
  }
}
