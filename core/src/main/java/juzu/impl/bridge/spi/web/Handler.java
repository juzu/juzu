/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.bridge.spi.web;

import juzu.PropertyType;
import juzu.Response;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.Parameters;
import juzu.impl.common.MethodHandle;
import juzu.impl.common.Tools;
import juzu.impl.common.URIWriter;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.plugin.router.RouteDescriptor;
import juzu.impl.plugin.router.RouterPlugin;
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
  private static final Phase[] OTHER_PHASES = {Phase.RESOURCE};

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
    RouterPlugin router = bridge.application.getPlugin(RouterPlugin.class);
    if (router != null) {
      RouteDescriptor route = router.getDescriptor();
      if (route != null) {
        Map<RouteDescriptor, Route> ret = route.create();
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
        if (juzu.Method.GET == bridge.getHttpContext().getMethod()) {
          phases = GET_PHASES;
        } else if (juzu.Method.POST == bridge.getHttpContext().getMethod()) {
          phases = POST_PHASES;
        } else {
          phases = OTHER_PHASES;
        }
        for (Phase phase : phases) {
          MethodHandle handle = m.get(phase);
          if (handle != null) {
            requestMethod =  this.bridge.application.getPlugin(ControllerPlugin.class).getDescriptor().getMethodByHandle(handle);
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
      requestMethod = this.bridge.application.getPlugin(ControllerPlugin.class).getResolver().resolve(Phase.VIEW, Collections.<String>emptySet());
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
          Phase.View.Dispatch update = (Phase.View.Dispatch)response;
          Boolean redirect = response.getProperties().getValue(PropertyType.REDIRECT_AFTER_ACTION);
          if (redirect != null && !redirect) {
            Method<?> desc = this.bridge.application.getPlugin(ControllerPlugin.class).getDescriptor().getMethodByHandle(update.getTarget());
            requestBridge = new WebRenderBridge(this.bridge, this, bridge, desc, Tools.toHashMap((Parameters)update.getParameters()));
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
