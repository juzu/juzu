package juzu.impl.bridge;

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
  public final String appName;

  /** . */
  public final InjectImplementation injectImpl;

  public BridgeConfig(int mode, String appName, InjectImplementation injectImpl) {
    this.mode = mode;
    this.appName = appName;
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
    this.appName = config.get("juzu.app_name");
    this.mode = mode;
    this.injectImpl = injectImpl;
  }

  public boolean isProd() {
    return mode == STATIC_MODE;
  }
}
