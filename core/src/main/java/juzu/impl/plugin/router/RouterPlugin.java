package juzu.impl.plugin.router;

import juzu.impl.common.JSON;
import juzu.impl.common.MethodHandle;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouterPlugin extends Plugin {

  public RouterPlugin() {
    super("router");
  }

  @Override
  public Descriptor init(ClassLoader loader, JSON config) throws Exception {
    List<RouteDescriptor> routes = new ArrayList<RouteDescriptor>();
    for (JSON route : config.getList("routes", JSON.class)) {
      String target = route.getString("target");
      String path = route.getString("path");
      RouteDescriptor r = new RouteDescriptor(MethodHandle.parse(target), path);
      routes.add(r);
    }
    return new RouterDescriptor(Collections.unmodifiableList(routes));
  }
}
