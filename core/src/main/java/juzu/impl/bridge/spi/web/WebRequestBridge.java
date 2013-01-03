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

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.impl.common.MimeType;
import juzu.impl.plugin.application.Application;
import juzu.impl.common.MethodHandle;
import juzu.impl.request.Method;
import juzu.impl.inject.Scoped;
import juzu.impl.inject.ScopedContext;
import juzu.impl.request.Argument;
import juzu.impl.request.Request;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.common.Tools;
import juzu.impl.router.Route;
import juzu.impl.router.RouteMatch;
import juzu.impl.common.URIWriter;
import juzu.impl.bridge.spi.DispatchSPI;
import juzu.request.HttpContext;
import juzu.request.Phase;
import juzu.request.SecurityContext;
import juzu.request.WindowContext;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class WebRequestBridge implements RequestBridge, WindowContext {

  /** . */
  final Application application;

  /** . */
  final Handler handler;

  /** . */
  final WebBridge http;

  /** . */
  final Map<String, String[]> parameters;

  /** . */
  final Method<?> target;

  /** . */
  final Map<String, ? extends Argument> arguments;

  /** . */
  protected Request request;

  /** . */
  protected Map<String, String[]> responseHeaders;

  WebRequestBridge(
      Application application,
      Handler handler,
      WebBridge http,
      Method<?> target,
      Map<String, String[]> parameters) {

    //
    this.arguments = target.getArguments(parameters);
    this.application = application;
    this.target = target;
    this.handler = handler;
    this.http = http;
    this.parameters = parameters;
    this.request = null;
  }

  //

  public MethodHandle getTarget() {
    return target.getHandle();
  }

  public Map<String, ? extends Argument> getArguments() {
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

  public final Map<String, String[]> getParameters() {
    return parameters;
  }

  public final HttpContext getHttpContext() {
    return http.getHttpContext();
  }

  public final WindowContext getWindowContext() {
    return this;
  }

  public final SecurityContext getSecurityContext() {
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

  public final DispatchSPI createDispatch(Phase phase, final MethodHandle target, final Map<String, String[]> parameters) {
    Method method = application.getDescriptor().getControllers().getMethodByHandle(target);

    //
    Route route = handler.getRoute(method.getHandle());
    if (route == null) {
      if (application.getDescriptor().getControllers().getResolver().isIndex(method)) {
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
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
          params.put(entry.getKey(), entry.getValue()[0]);
        }
      }

      //
      final RouteMatch match = route.matches(params);
      if (match != null) {
        return new DispatchSPI() {

          public MethodHandle getTarget() {
            return target;
          }

          public Map<String, String[]> getParameters() {
            return parameters;
          }

          public <T> String checkPropertyValidity(PropertyType<T> propertyType, T propertyValue) {
            // For now we don't validate anything
            return null;
          }

          public void renderURL(PropertyMap properties, MimeType mimeType, Appendable appendable) throws IOException {

            // Render base URL
            http.renderRequestURL(appendable);

            //
            URIWriter writer = new URIWriter(appendable, mimeType);
            match.render(writer);
            for (Map.Entry<String, String> entry : match.getUnmatched().entrySet()) {
              String[] values = parameters.get(entry.getKey());
              for (String value : values) {
                writer.appendQueryParameter(entry.getKey(), value);
              }
            }
          }
        };
      } else {
        StringBuilder msg = new StringBuilder("The parameters ");
        Tools.toString(parameters.entrySet(), msg);
        msg.append(" are not valid");
        throw new IllegalArgumentException(msg.toString());
      }
    } else {
      throw new UnsupportedOperationException("handle me gracefully method not mapped " + method.getHandle());
    }
  }

  public void setResponse(Response response) throws IllegalArgumentException, IOException {
    responseHeaders = Collections.emptyMap();
    Iterable<Map.Entry<String, String[]>> headers = response.getProperties().getValues(PropertyType.HEADER);
    if (headers != null) {
      for (Map.Entry<String, String[]> entry : headers) {
        if (responseHeaders.isEmpty()) {
          responseHeaders = new HashMap<String, String[]>();
        }
        responseHeaders.put(entry.getKey(), entry.getValue());
      }
    }
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

  /**
   * Send the response to the client.
   */
  void send() throws IOException {
  }
}
