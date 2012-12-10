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

package juzu.impl.bridge.spi.servlet;

import juzu.impl.bridge.Bridge;
import juzu.impl.common.MethodHandle;
import juzu.impl.plugin.router.RouteDescriptor;
import juzu.impl.router.Route;
import juzu.request.Phase;

import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class Handler {

  /** . */
  final Bridge bridge;

  /** . */
  final Route root;

  /** . */
  final HashMap<MethodHandle, Route> routeMap;

  /** . */
  final HashMap<Route, Map<Phase, MethodHandle>> routeMap2;

  /** All the routes. */
  final HashSet<Route> routes;

  Handler(Route parent, Bridge bridge) throws ServletException {
    this.bridge = bridge;

    //
    try {
      bridge.boot();

      //
      HashMap<MethodHandle, Route> routeMap = new HashMap<MethodHandle, Route>();
      HashMap<Route, Map<Phase, MethodHandle>> routeMap2 = new HashMap<Route, Map<Phase, MethodHandle>>();
      HashSet<Route> routes = new HashSet<Route>();
      Route root;

      //
      RouteDescriptor routesDesc = (RouteDescriptor)bridge.runtime.getDescriptor().getPluginDescriptor("router");
      if (routesDesc != null) {
        Map<RouteDescriptor, Route> ret = routesDesc.popupate(parent);
        root = ret.values().iterator().next();
        for (Map.Entry<RouteDescriptor, Route> entry : ret.entrySet()) {
          for (Map.Entry<String, String> entry2 : entry.getKey().getTargets().entrySet()) {
            MethodHandle handle = MethodHandle.parse(entry2.getValue());
            Phase phase = Phase.valueOf(entry2.getKey());
            routeMap.put(handle, entry.getValue());
            Map<Phase, MethodHandle> map =  routeMap2.get(entry.getValue());
            if (map == null) {
              routeMap2.put(entry.getValue(), map = new HashMap<Phase, MethodHandle>());
            }
            map.put(phase, handle);
          }
          routes.add(entry.getValue());
        }
      } else {
        routes.add(root = parent.append("/" + bridge.runtime.getName().getLastName()));
      }

      //
      this.routes = routes;
      this.root = root;
      this.routeMap = routeMap;
      this.routeMap2 = routeMap2;
    }
    catch (Exception e) {
      throw ServletBridge.wrap(e);
    }
  }
}
