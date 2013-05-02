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

package juzu.test.protocol.http;

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.impl.bridge.Parameters;
import juzu.impl.bridge.spi.DispatchBridge;
import juzu.impl.common.Logger;
import juzu.impl.common.MimeType;
import juzu.impl.common.MethodHandle;
import juzu.impl.plugin.application.ApplicationLifeCycle;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.request.Method;
import juzu.impl.inject.Scoped;
import juzu.impl.inject.ScopedContext;
import juzu.impl.request.Parameter;
import juzu.impl.request.Request;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.common.Tools;
import juzu.request.ApplicationContext;
import juzu.request.ClientContext;
import juzu.request.HttpContext;
import juzu.request.Phase;
import juzu.request.SecurityContext;
import juzu.request.UserContext;
import juzu.request.WindowContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class RequestBridgeImpl implements RequestBridge, HttpContext, WindowContext, ClientContext {

  /** . */
  final ApplicationLifeCycle<?, ?> application;

  /** . */
  final HttpServletRequest req;

  /** . */
  final HttpServletResponse resp;

  /** . */
  final Map<String, String[]> parameters;

  /** . */
  final MethodHandle target;

  /** . */
  final Map<Parameter, Object> arguments;

  /** . */
  protected Request request;

  /** . */
  private final juzu.Method method;

  /** . */
  private final Logger log;

  RequestBridgeImpl(
      Logger log,
      ApplicationLifeCycle<?, ?> application,
      HttpServletRequest req,
      HttpServletResponse resp,
      MethodHandle target,
      Map<String, String[]> parameters) {

    Method<?> desc = application.getPlugin(ControllerPlugin.class).getDescriptor().getMethodByHandle(target);
    Map<Parameter, Object> arguments = desc.getArguments(parameters);

    //
    this.application = application;
    this.req = req;
    this.resp = resp;
    this.target = target;
    this.parameters = parameters;
    this.request = null;
    this.arguments = arguments;
    this.method = juzu.Method.valueOf(req.getMethod());
    this.log = log;
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

  public juzu.Method getMethod() {
    return method;
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

  public MethodHandle getTarget() {
    return target;
  }

  public Map<Parameter, Object> getArguments() {
    return arguments;
  }

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

  public UserContext getUserContext() {
    return null;
  }

  public ApplicationContext getApplicationContext() {
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
      for (String key : Tools.list((Enumeration<String>)session.getAttributeNames())) {
        session.removeAttribute(key);
      }
    }
  }

  public final DispatchBridge createDispatch(Phase phase, final MethodHandle target, final Parameters parameters) throws NullPointerException, IllegalArgumentException {
    return new DispatchBridge() {

      public MethodHandle getTarget() {
        return target;
      }

      public Parameters getParameters() {
        return parameters;
      }

      public <T> String checkPropertyValidity(PropertyType<T> propertyType, T propertyValue) {
        // For now we don't validate anything
        return null;
      }

      public void renderURL(PropertyMap properties, MimeType mimeType, Appendable appendable) throws IOException {

        //
        Method method = application.getPlugin(ControllerPlugin.class).getDescriptor().getMethodByHandle(target);

        //
        appendable.append(req.getScheme());
        appendable.append("://");
        appendable.append(req.getServerName());
        int port = req.getServerPort();
        if (port != 80) {
          appendable.append(':').append(Integer.toString(port));
        }
        appendable.append(req.getContextPath());
        appendable.append(req.getServletPath());
        appendable.append("?juzu.phase=").append(method.getPhase().name());

        //
        appendable.append("&juzu.op=").append(method.getId());

        //
        for (juzu.impl.bridge.Parameter parameter : parameters.values()) {
          String name = parameter.getName();
          try {
            String encName = URLEncoder.encode(name, "UTF-8");
            for (int i = 0;i < parameter.getSize();i++) {
              String value = parameter.getValue(i);
              String encValue = URLEncoder.encode(value, "UTF-8");
              appendable.append("&").append(encName).append('=').append(encValue);
            }
          }
          catch (UnsupportedEncodingException e) {
            // Should not happen
            throw new AssertionError(e);
          }
        }
      }
    };
  }

  protected final ScopedContext getRequestContext(boolean create) {
    ScopedContext context = (ScopedContext)req.getAttribute("juzu.request_scope");
    if (context == null && create) {
      req.setAttribute("juzu.request_scope", context = new ScopedContext(log));
    }
    return context;
  }

  protected final ScopedContext getFlashContext(boolean create) {
    ScopedContext context = null;
    HttpSession session = req.getSession(create);
    if (session != null) {
      context = (ScopedContext)session.getAttribute("juzu.flash_scope");
      if (context == null && create) {
        session.setAttribute("juzu.flash_scope", context = new ScopedContext(log));
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
        session.setAttribute("juzu.session_scope", context = new ScopedContext(log));
      }
    }
    return context;
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
}
