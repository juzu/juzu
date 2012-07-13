package juzu.impl.bridge;

import juzu.impl.inject.spi.InjectImplementation;
import juzu.impl.common.Tools;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BridgeConfig {

  /** . */
  public static final String RUN_MODE = "juzu.run_mode";

  /** . */
  public static final String INJECT = "juzu.inject";

  /** . */
  public static final String APP_NAME = "juzu.app_name";

  /** . */
  public static final Set<String> NAMES = Collections.unmodifiableSet(Tools.set(RUN_MODE, INJECT, APP_NAME));

  /** . */
  public final boolean prod;

  /** . */
  public final String appName;

  /** . */
  public final InjectImplementation injectImpl;

  public BridgeConfig(boolean prod, String appName, InjectImplementation injectImpl) {
    this.prod = prod;
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
    this.appName = config.get("juzu.app_name");
    this.prod = !("dev".equals(runMode));
    this.injectImpl = injectImpl;
  }
}
