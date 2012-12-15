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

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.impl.common.MimeType;
import juzu.impl.plugin.application.ApplicationContext;
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
import juzu.request.ClientContext;
import juzu.impl.bridge.spi.DispatchSPI;
import juzu.request.HttpContext;
import juzu.request.Phase;
import juzu.request.SecurityContext;
import juzu.request.WindowContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ServletRequestBridge implements RequestBridge, HttpContext, WindowContext, ClientContext {

  /** . */
  final ApplicationContext application;

  /** . */
  final Handler handler;

  /** . */
  final HttpServletRequest req;

  /** . */
  final HttpServletResponse resp;

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

  ServletRequestBridge(
      ApplicationContext application,
      Handler handler,
      HttpServletRequest req,
      HttpServletResponse resp,
      Method<?> target,
      Map<String, String[]> parameters) {

    //
    this.arguments = target.getArguments(parameters);
    this.application = application;
    this.target = target;
    this.handler = handler;
    this.req = req;
    this.resp = resp;
    this.parameters = parameters;
    this.request = null;
  }

  //

  public String getContentType() {
    return req.getContentType();
  }

  public String getCharacterEncoding() {
    return req.getCharacterEncoding();
  }

  public int getContentLenth() {
    return req.getContentLength();
  }

  public InputStream getInputStream() throws IOException {
    return req.getInputStream();
  }

  //

  public String getMethod() {
    return req.getMethod();
  }

  public MethodHandle getTarget() {
    return target.getHandle();
  }

  public Map<String, ? extends Argument> getArguments() {
    return arguments;
  }

  public Cookie[] getCookies() {
    return req.getCookies();
  }

  public String getScheme() {
    return req.getScheme();
  }

  public int getServerPort() {
    return req.getServerPort();
  }

  public String getServerName() {
    return req.getServerName();
  }

  public String getContextPath() {
    return req.getContextPath();
  }

  public <T> T getProperty(PropertyType<T> propertyType) {
    if (PropertyType.PATH.equals(propertyType)) {
      return propertyType.cast(req.getRequestURI());
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
    return this;
  }

  public final WindowContext getWindowContext() {
    return this;
  }

  public final SecurityContext getSecurityContext() {
    return null;
  }

  public final Scoped getRequestValue(Object key) {
    ScopedContext context = getRequestContext(false);
    return context != null ? context.get(key) : null;
  }

  public final void setRequestValue(Object key, Scoped value) {
    if (value != null) {
      ScopedContext context = getRequestContext(false);
      if (context != null) {
        context.set(key, null);
      }
    }
    else {
      getRequestContext(true).set(key, value);
    }
  }

  public final Scoped getFlashValue(Object key) {
    ScopedContext context = getFlashContext(false);
    return context != null ? context.get(key) : null;
  }

  public final void setFlashValue(Object key, Scoped value) {
    if (value == null) {
      ScopedContext context = getFlashContext(false);
      if (context != null) {
        context.set(key, null);
      }
    }
    else {
      getFlashContext(true).set(key, value);
    }
  }

  public final Scoped getSessionValue(Object key) {
    ScopedContext context = getSessionContext(false);
    return context != null ? context.get(key) : null;
  }

  public final void setSessionValue(Object key, Scoped value) {
    if (value == null) {
      ScopedContext context = getSessionContext(false);
      if (context != null) {
        context.set(key, null);
      }
    }
    else {
      getSessionContext(true).set(key, value);
    }
  }

  public final Scoped getIdentityValue(Object key) {
    return null;
  }

  public final void setIdentityValue(Object key, Scoped value) {
  }

  public void purgeSession() {
    HttpSession session = req.getSession(false);
    if (session != null) {
      for (String key : Tools.list(session.getAttributeNames())) {
        session.removeAttribute(key);
      }
    }
  }

  public final DispatchSPI createDispatch(Phase phase, final MethodHandle target, final Map<String, String[]> parameters) {
    Method method = application.getDescriptor().getControllers().getMethodByHandle(target);

    //
    Route route = handler.routeMap.get(method.getHandle());
    if (route == null) {
      if (application.getDescriptor().getControllers().getResolver().isIndex(method)) {
        route = handler.root;
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

            //
            appendable.append(req.getScheme());
            appendable.append("://");
            appendable.append(req.getServerName());
            int port = req.getServerPort();
            if (port != 80) {
              appendable.append(':').append(Integer.toString(port));
            }

            // Add context path
            appendable.append(req.getContextPath());

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

  protected final ScopedContext getRequestContext(boolean create) {
    ScopedContext context = (ScopedContext)req.getAttribute("juzu.request_scope");
    if (context == null && create) {
      req.setAttribute("juzu.request_scope", context = new ScopedContext());
    }
    return context;
  }

  protected final ScopedContext getFlashContext(boolean create) {
    ScopedContext context = null;
    HttpSession session = req.getSession(create);
    if (session != null) {
      context = (ScopedContext)session.getAttribute("juzu.flash_scope");
      if (context == null && create) {
        session.setAttribute("juzu.flash_scope", context = new ScopedContext());
      }
    }
    return context;
  }

  protected final ScopedContext getSessionContext(boolean create) {
    ScopedContext context = null;
    HttpSession session = req.getSession(create);
    if (session != null) {
      context = (ScopedContext)session.getAttribute("juzu.session_scope");
      if (context == null && create) {
        session.setAttribute("juzu.session_scope", context = new ScopedContext());
      }
    }
    return context;
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
    ScopedContext context = getRequestContext(false);
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
