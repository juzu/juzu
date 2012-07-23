package juzu.impl.plugin.router;

import juzu.impl.common.MethodHandle;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteDescriptor {

  /** . */
  private final MethodHandle target;

  /** . */
  private final String path;

  RouteDescriptor(MethodHandle target, String path) {
    this.target = target;
    this.path = path;
  }

  public MethodHandle getTarget() {
    return target;
  }

  public String getPath() {
    return path;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[path=" + path + ",target=" + target + "]";
  }
}
