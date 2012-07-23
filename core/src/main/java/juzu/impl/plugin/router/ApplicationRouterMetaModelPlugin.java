package juzu.impl.plugin.router;

import juzu.impl.application.metamodel.ApplicationMetaModel;
import juzu.impl.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.application.metamodel.ApplicationsMetaModel;
import juzu.impl.common.JSON;
import juzu.impl.common.MethodHandle;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.controller.metamodel.MethodMetaModel;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.metamodel.MetaModelObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationRouterMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  private final HashMap<ElementHandle.Method, String> routes = new HashMap<ElementHandle.Method, String>();

  public ApplicationRouterMetaModelPlugin() {
    super("router");
  }

  @Override
  public void processEvent(ApplicationsMetaModel applications, MetaModelEvent event) {
    if (event.getType() == MetaModelEvent.AFTER_ADD) {
      MetaModelObject mmo = event.getObject();
      if (mmo instanceof MethodMetaModel) {
        MethodMetaModel mmm = (MethodMetaModel)mmo;
        if (mmm.getRoute() != null) {
          routes.put(mmm.getHandle(), mmm.getRoute());
        }
      }
    } else if (event.getType() == MetaModelEvent.BEFORE_REMOVE) {
      MetaModelObject mmo = event.getObject();
      if (mmo instanceof MethodMetaModel) {
        MethodMetaModel mmm = (MethodMetaModel)mmo;
        routes.remove(mmm.getHandle());
      }
    }
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {

    JSON descriptor = new JSON();

    // Build routes configuration
    ArrayList<JSON> routes = new ArrayList<JSON>();
    for (Map.Entry<ElementHandle.Method, String> entry : this.routes.entrySet()) {
      MethodHandle target = new MethodHandle(
          entry.getKey().getFQN().getName(),
          entry.getKey().getName(),
          entry.getKey().getParameterTypes().toArray(new String[entry.getKey().getParameterTypes().size()])
      );
      routes.add(new JSON().
          set("path", entry.getValue()).
          set("target", target.toString())
      );
    }
    descriptor.set("routes", routes);

    return descriptor;
  }
}
