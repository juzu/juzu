package juzu.impl.plugin.router;

import juzu.impl.metadata.Descriptor;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouterDescriptor extends Descriptor {

  /** . */
  private final List<RouteDescriptor> routes;

  RouterDescriptor(List<RouteDescriptor> routes) {
    this.routes = routes;
  }

  public List<RouteDescriptor> getRoutes() {
    return routes;
  }
}
