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

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.impl.asset.Asset;
import juzu.impl.bridge.Bridge;
import juzu.impl.request.ControlParameter;
import juzu.request.RequestParameter;
import juzu.request.ResponseParameter;
import juzu.impl.bridge.spi.DispatchBridge;
import juzu.impl.common.MimeType;
import juzu.impl.common.MethodHandle;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.request.Method;
import juzu.impl.inject.Scoped;
import juzu.impl.inject.ScopedContext;
import juzu.impl.request.Request;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.common.Tools;
import juzu.impl.router.PathParam;
import juzu.impl.router.Route;
import juzu.impl.router.RouteMatch;
import juzu.impl.common.URIWriter;
import juzu.request.ApplicationContext;
import juzu.request.HttpContext;
import juzu.request.Phase;
import juzu.request.SecurityContext;
import juzu.request.UserContext;
import juzu.request.WindowContext;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class WebRequestBridge implements RequestBridge, WindowContext {

  /** . */
  final Bridge bridge;

  /** . */
  final Handler handler;

  /** . */
  final WebBridge http;

  /** . */
  final Method<?> target;

  /** . */
  final Map<ControlParameter, Object> arguments;

  /** . */
  protected Request request;

  /** . */
  protected UserContext userContext;

  /** . */
  protected Map<String, RequestParameter> requestParameters;

  /** . */
  protected Response response;

  WebRequestBridge(
      Bridge bridge,
      Handler handler,
      WebBridge http,
      Method<?> target,
      Map<String, RequestParameter> requestParameters) {

    //
    this.arguments = target.getArguments(requestParameters);
    this.requestParameters = requestParameters;
    this.bridge = bridge;
    this.target = target;
    this.handler = handler;
    this.http = http;
    this.request = null;
  }

  //

  public Map<String, RequestParameter> getRequestParameters() {
    return requestParameters;
  }

  public MethodHandle getTarget() {
    return target.getHandle();
  }

  public Map<ControlParameter, Object> getArguments() {
    return arguments;
  }

  public <T> T getProperty(PropertyType<T> propertyType) {
    if (PropertyType.PATH.equals(propertyType)) {
      return propertyType.cast(http.getRequestURI());
    }
    return null;
  }

  //

  public final String getNamespace() {
    return "window_ns";
  }

  public final String getId() {
    return "window_id";
  }
  //

  public final HttpContext getHttpContext() {
    return http.getHttpContext();
  }

  public final WindowContext getWindowContext() {
    return this;
  }

  public final SecurityContext getSecurityContext() {
    return null;
  }

  public UserContext getUserContext() {
    return http.getUserContext();
  }

  public ApplicationContext getApplicationContext() {
    return null;
  }

  public final Scoped getRequestValue(Object key) {
    ScopedContext context = http.getRequestScope(false);
    return context != null ? context.get(key) : null;
  }

  public final void setRequestValue(Object key, Scoped value) {
    if (value != null) {
      ScopedContext context = http.getRequestScope(false);
      if (context != null) {
        context.set(key, null);
      }
    }
    else {
      http.getRequestScope(true).set(key, value);
    }
  }

  public final Scoped getFlashValue(Object key) {
    ScopedContext context = http.getFlashScope(false);
    return context != null ? context.get(key) : null;
  }

  public final void setFlashValue(Object key, Scoped value) {
    if (value == null) {
      ScopedContext context = http.getFlashScope(false);
      if (context != null) {
        context.set(key, null);
      }
    }
    else {
      http.getFlashScope(true).set(key, value);
    }
  }

  public final Scoped getSessionValue(Object key) {
    ScopedContext context = http.getSessionScope(false);
    return context != null ? context.get(key) : null;
  }

  public final void setSessionValue(Object key, Scoped value) {
    if (value == null) {
      ScopedContext context = http.getSessionScope(false);
      if (context != null) {
        context.set(key, null);
      }
    }
    else {
      http.getSessionScope(true).set(key, value);
    }
  }

  public final Scoped getIdentityValue(Object key) {
    return null;
  }

  public final void setIdentityValue(Object key, Scoped value) {
  }

  public void purgeSession() {
    http.purgeSession();
  }

  public final DispatchBridge createDispatch(Phase phase, final MethodHandle target, final Map<String, ResponseParameter> parameters) {
    Method method = bridge.application.getPlugin(ControllerPlugin.class).getDescriptor().getMethodByHandle(target);

    //
    Route route = handler.getRoute(method.getHandle());
    if (route == null) {
      if (bridge.application.getPlugin(ControllerPlugin.class).getResolver().isIndex(method)) {
        route = handler.getRoot();
      }
    }

    //
    if (route != null) {
      Map<String, String> params;
      if (parameters.isEmpty()) {
        params = Collections.emptyMap();
      } else {
        params = new HashMap<String, String>(parameters.size());
        for (ResponseParameter parameter : parameters.values()) {
          params.put(parameter.getName(), parameter.get(0));
        }
      }

      //
      final RouteMatch match = route.matches(params);
      if (match != null) {
        return new DispatchBridge() {

          public MethodHandle getTarget() {
            return target;
          }

          public Map<String, ResponseParameter> getParameters() {
            return parameters;
          }

          public <T> String checkPropertyValidity(PropertyType<T> propertyType, T propertyValue) {
            // For now we don't validate anything
            return null;
          }

          public void renderURL(PropertyMap properties, MimeType mimeType, Appendable appendable) throws IOException {

            // Render base URL
            http.renderRequestURL(appendable);

            // Render path
            URIWriter writer = new URIWriter(appendable, mimeType);
            match.render(writer);

            // Retain matched parameters for filtering later
            Set<String> matched = match.getMatched().isEmpty() ? Collections.<String>emptySet() : new HashSet<String>(match.getMatched().size());
            for (PathParam param : match.getMatched().keySet()) {
              matched.add(param.getName());
            }

            // Render remaining parameters which have not been rendered yet
            for (ResponseParameter parameter : parameters.values()) {
              if (!matched.contains(parameter.getName())) {
                for (int i = 0;i < parameter.size();i++) {
                  writer.appendQueryParameter(parameter.getEncoding(), parameter.getName(), parameter.get(i));
                }
              }
            }
          }
        };
      } else {
        throw new IllegalArgumentException("The parameters " + parameters + " are not valid");
      }
    } else {
      throw new UnsupportedOperationException("handle me gracefully method not mapped " + method.getHandle());
    }
  }

  public void setResponse(Response response) throws IllegalArgumentException, IOException {
    this.response = response;
  }

  public final void begin(Request request) {
    this.request = request;
  }

  public void end() {
    this.request = null;

    //
    ScopedContext context = http.getRequestScope(false);
    if (context != null) {
      context.close();
    }
  }

  public void close() {
  }

  void invoke() throws Exception {
    try {
      bridge.application.getPlugin(ControllerPlugin.class).invoke(this);
    } finally {
      Tools.safeClose(this);
    }
  }

  /**
   * Send the response to the client.
   */
  boolean send() throws IOException {
    if (response instanceof Response.Error) {
      Response.Error error = (Response.Error)response;
      http.send(error, bridge.module.context.getRunMode().getPrettyFail());
      return true;
    } else if (response instanceof Response.View) {
      Response.View update = (Response.View)response;
      String url = update.with(MimeType.PLAIN).with(update.getProperties()).toString();
      Iterable<Map.Entry<String, String[]>> headers = response.getProperties().getValues(PropertyType.HEADER);
      if (headers != null) {
        for (Map.Entry<String, String[]> entry : headers) {
          http.setHeader(entry.getKey(), entry.getValue()[0]);
        }
      }
      http.sendRedirect(url);
      return true;
    }
    else if (response instanceof Response.Redirect) {
      Response.Redirect redirect = (Response.Redirect)response;
      String url = redirect.getLocation();
      http.sendRedirect(url);
      return true;
    } else {
      return false;
    }
  }

  private String getAssetURL(Asset asset) throws IOException {
    StringBuilder url = new StringBuilder();
    String uri = asset.getURI();
    http.renderAssetURL(asset.getLocation(), uri, url);
    return url.toString();
  }
}
