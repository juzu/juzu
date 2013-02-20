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

package juzu.impl.bridge.spi.web;

import juzu.PropertyType;
import juzu.Response;
import juzu.impl.bridge.Bridge;
import juzu.impl.common.MethodHandle;
import juzu.impl.common.URIWriter;
import juzu.impl.plugin.router.RouteDescriptor;
import juzu.impl.request.Method;
import juzu.impl.router.PathParam;
import juzu.impl.router.Route;
import juzu.impl.router.RouteMatch;
import juzu.impl.router.Router;
import juzu.request.Phase;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Handler implements Closeable {

  /** . */
  private static final Phase[] GET_PHASES = {Phase.VIEW, Phase.ACTION, Phase.RESOURCE};

  /** . */
  private static final Phase[] POST_PHASES = {Phase.ACTION, Phase.VIEW, Phase.RESOURCE};

  /** . */
  final Bridge bridge;

  /** . */
  final Route root;

  /** . */
  final HashMap<MethodHandle, Route> forwardRoutes;

  /** . */
  final HashMap<Route, Map<Phase, MethodHandle>> backwardRoutes;

  public Handler(Bridge bridge) throws Exception {
    this.bridge = bridge;

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
    this.forwardRoutes = forwardRoutes;
    this.backwardRoutes = backwardRoutes;
    this.root = root;
  }

  public Map<Phase, MethodHandle> getMethods(Route route) {
    return backwardRoutes.get(route);
  }

  public Route getRoute(MethodHandle method) {
    return forwardRoutes.get(method);
  }

  public Route getRoot() {
    return root;
  }

  public Bridge getBridge() {
    return bridge;
  }

  public void handle(WebBridge bridge) throws Throwable {

    //
    String requestPath = bridge.getRequestPath();

    // Determine first a possible match from the root route from the request path
    RouteMatch requestMatch = null;
    if (requestPath.startsWith(bridge.getPath())) {
      requestMatch = root.route(requestPath.substring(bridge.getPath().length()), Collections.<String, String[]>emptyMap());
    }

    // Determine a method + parameters if we have a match
    Method requestMethod = null;
    Map<String, String[]> requestParameters = Collections.emptyMap();
    if (requestMatch != null) {
      Map<Phase, MethodHandle> m = getMethods(requestMatch.getRoute());
      if (m != null) {
        Phase[] phases;
        if ("GET".equals(bridge.getHttpContext().getMethod())) {
          phases = GET_PHASES;
        } else if ("POST".equals(bridge.getHttpContext().getMethod())) {
          phases = POST_PHASES;
        } else {
          throw new UnsupportedOperationException("handle me gracefully");
        }
        for (Phase phase : phases) {
          MethodHandle handle = m.get(phase);
          if (handle != null) {
            requestMethod =  this.bridge.application.getDescriptor().getControllers().getMethodByHandle(handle);
            if (requestMatch.getMatched().size() > 0 || bridge.getParameters().size() > 0) {
              requestParameters = new HashMap<String, String[]>();
              for (Map.Entry<String, String[]> entry : bridge.getParameters().entrySet()) {
                requestParameters.put(entry.getKey(), entry.getValue().clone());
              }
              for (Map.Entry<PathParam, String> entry : requestMatch.getMatched().entrySet()) {
                requestParameters.put(entry.getKey().getName(), new String[]{entry.getValue()});
              }
            }
            break;
          }
        }
      }
    }

    // No method means we either send a server resource
    // or we look for the handler method
    if (requestMethod == null) {
      // If we have an handler we locate the index method
      requestMethod = this.bridge.application.getDescriptor().getControllers().getResolver().resolve(Phase.VIEW, Collections.<String>emptySet());
    }

    // No method -> not found
    if (requestMethod == null) {
      bridge.setStatus(404);
    } else {
      if (requestMatch == null) {
        Route requestRoute = getRoute(requestMethod.getHandle());
        if (requestRoute != null) {
          requestMatch = requestRoute.matches(Collections.<String, String>emptyMap());
          if (requestMatch != null) {
            StringBuilder sb = new StringBuilder();
            requestMatch.render(new URIWriter(sb));
            if (!sb.toString().equals(requestPath)) {
              StringBuilder redirect = new StringBuilder();
              bridge.renderRequestURL(redirect);
              redirect.append(sb);
              bridge.sendRedirect(redirect.toString());
              return;
            }
          }
        }
      }

      //
      WebRequestBridge requestBridge;
      if (requestMethod.getPhase() == Phase.ACTION) {
        requestBridge = new WebActionBridge(this.bridge, this, bridge, requestMethod, requestParameters);
      } else if (requestMethod.getPhase() == Phase.VIEW) {
        requestBridge = new WebRenderBridge(this.bridge, this, bridge, requestMethod, requestParameters);
      } else if (requestMethod.getPhase() == Phase.RESOURCE) {
        requestBridge = new WebResourceBridge(this.bridge, this, bridge, requestMethod, requestParameters);
      } else {
        throw new Exception("Cannot decode phase");
      }

      //
      requestBridge.invoke();

      // Implement the two phases in one
      if (requestBridge instanceof WebActionBridge) {
        Response response = ((WebActionBridge)requestBridge).response;
        if (response instanceof Response.View) {
          Response.View update = (Response.View)response;
          Boolean redirect = response.getProperties().getValue(PropertyType.REDIRECT_AFTER_ACTION);
          if (redirect != null && !redirect) {
            Method<?> desc = this.bridge.application.getDescriptor().getControllers().getMethodByHandle(update.getTarget());
            requestBridge = new WebRenderBridge(this.bridge, this, bridge, desc, update.getParameters());
            requestBridge.invoke();
          }
        }
      }

      //
      if (requestBridge.send()) {
        // ok
      } else {
        throw new UnsupportedOperationException("Not yet handled by " + requestBridge.getClass().getSimpleName() + ": " + requestBridge.response);
      }
    }
  }

  public void close() throws IOException {
  }
}
