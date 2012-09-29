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

package juzu.impl.bridge.spi.standalone;

import juzu.impl.bridge.Bridge;
import juzu.impl.common.MethodHandle;
import juzu.impl.plugin.router.RouteDescriptor;
import juzu.impl.router.Route;
import juzu.request.Phase;

import javax.servlet.ServletException;
import java.util.HashMap;
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

  Handler(Route root, Bridge bridge) throws ServletException {
    this.bridge = bridge;

    //
    try {
      bridge.boot();

      //
      HashMap<MethodHandle, Route> routeMap = new HashMap<MethodHandle, Route>();
      HashMap<Route, Map<Phase, MethodHandle>> routeMap2 = new HashMap<Route, Map<Phase, MethodHandle>>();

      //
      RouteDescriptor routesDesc = (RouteDescriptor)bridge.runtime.getDescriptor().getPluginDescriptor("router");
      if (routesDesc != null) {
        for (RouteDescriptor child : routesDesc.getChildren()) {
          Route route = root.append(child.getPath());
          for (Map.Entry<String, String> entry : child.getTargets().entrySet()) {
            MethodHandle handle = MethodHandle.parse(entry.getValue());
            Phase phase = Phase.valueOf(entry.getKey());
            routeMap.put(handle, route);
            Map<Phase, MethodHandle> map =  routeMap2.get(route);
            if (map == null) {
              routeMap2.put(route, map = new HashMap<Phase, MethodHandle>());
            }
            map.put(phase, handle);
          }
        }
      }

      //
      this.root = root;
      this.routeMap = routeMap;
      this.routeMap2 = routeMap2;
    }
    catch (Exception e) {
      throw ServletBridge.wrap(e);
    }
  }
}
