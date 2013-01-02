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
import juzu.impl.common.Tools;
import juzu.impl.plugin.router.RouteDescriptor;
import juzu.impl.router.Route;
import juzu.impl.router.Router;
import juzu.request.Phase;

import javax.servlet.ServletException;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class Handler implements Closeable {

  /** . */
  final String path;

  /** . */
  final Bridge bridge;

  /** . */
  final Route root;

  /** . */
  final HashMap<MethodHandle, Route> forwardRoutes;

  /** . */
  final HashMap<Route, Map<Phase, MethodHandle>> backwardRoutes;

  Handler(Bridge bridge, String path) throws ServletException {
    this.bridge = bridge;

    //
    try {
      bridge.refresh();

      //
      HashMap<MethodHandle, Route> forwardRoutes = new HashMap<MethodHandle, Route>();
      HashMap<Route, Map<Phase, MethodHandle>> backwardRoutes = new HashMap<Route, Map<Phase, MethodHandle>>();

      //
      Route root;
      RouteDescriptor routesDesc = (RouteDescriptor)bridge.application.getDescriptor().getPluginDescriptor("router");
      if (routesDesc != null) {
        Map<RouteDescriptor, Route> ret;
        ret = routesDesc.create();
        root = ret.values().iterator().next();
        for (Map.Entry<RouteDescriptor, Route> entry : ret.entrySet()) {
          for (Map.Entry<String, String> entry2 : entry.getKey().getTargets().entrySet()) {
            MethodHandle handle = MethodHandle.parse(entry2.getValue());
            Phase phase = Phase.valueOf(entry2.getKey());
            forwardRoutes.put(handle, entry.getValue());
            Map<Phase, MethodHandle> map =  backwardRoutes.get(entry.getValue());
            if (map == null) {
              backwardRoutes.put(entry.getValue(), map = new HashMap<Phase, MethodHandle>());
            }
            map.put(phase, handle);
          }
        }
      } else {
        root = new Router();
      }

      //
      this.path = path;
      this.forwardRoutes = forwardRoutes;
      this.backwardRoutes = backwardRoutes;
      this.root = root;
    }
    catch (Exception e) {
      throw ServletBridge.wrap(e);
    }
  }

  public void close() throws IOException {
    Tools.safeClose(bridge);
  }
}
