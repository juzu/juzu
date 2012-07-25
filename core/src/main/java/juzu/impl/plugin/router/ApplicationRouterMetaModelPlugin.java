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

package juzu.impl.plugin.router;

import juzu.impl.application.metamodel.ApplicationMetaModel;
import juzu.impl.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.application.metamodel.ApplicationsMetaModel;
import juzu.impl.common.JSON;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.controller.metamodel.MethodMetaModel;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.metamodel.MetaModelObject;

import java.util.HashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationRouterMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  private final HashMap<ElementHandle.Package, RouteMetaModel> routes = new HashMap<ElementHandle.Package, RouteMetaModel>();

  public ApplicationRouterMetaModelPlugin() {
    super("router");
  }

  private RouteMetaModel route(ElementHandle.Package application, boolean create) {
    RouteMetaModel route = routes.get(application);
    if (route == null && create) {
      routes.put(application, route = new RouteMetaModel());
    }
    return route;
  }

  @Override
  public void processEvent(ApplicationsMetaModel applications, MetaModelEvent event) {
    MetaModelObject object = event.getObject();
    if (object instanceof MethodMetaModel) {
      MethodMetaModel method = (MethodMetaModel)object;
      if (event.getType() == MetaModelEvent.AFTER_ADD) {
        if (method.getRoute() != null) {
          ApplicationMetaModel application = method.getController().getControllers().getApplication();
          RouteMetaModel route = route(application.getHandle(), true).addChild(method.getRoute());
          route.setTarget(method.getPhase().name(), method.getHandle().getMethodHandle().toString());
        }
      } else if (event.getType() == MetaModelEvent.BEFORE_REMOVE) {
        ElementHandle.Package pkg = (ElementHandle.Package)event.getPayload();
        RouteMetaModel route = route(pkg, false);
        if (route != null) {
          route.setTarget(method.getPhase().name(), null);
        }
      }
    }
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    RouteMetaModel route = route(application.getHandle(), false);
    return route != null ? route.toJSON() : null;
  }
}
