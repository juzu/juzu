package juzu.impl.controller.descriptor;

import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteDescriptor {

  /** . */
  private final String id;

  /** . */
  private final Set<String> names;

  /** . */
  private final String path;

  RouteDescriptor(String id, Set<String> names, String path) {
    this.id = id;
    this.names = names;
    this.path = path;
  }

  public String getId() {
    return id;
  }

  public Set<String> getNames() {
    return names;
  }

  public String getPath() {
    return path;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[path=" + path + ",id=" + id + ",names=" + names + "]";
  }
}
