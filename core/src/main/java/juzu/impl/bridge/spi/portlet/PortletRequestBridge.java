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

package juzu.impl.bridge.spi.portlet;

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.Scope;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.spi.servlet.ServletScopedContext;
import juzu.impl.request.ControlParameter;
import juzu.request.RequestParameter;
import juzu.request.ResponseParameter;
import juzu.impl.bridge.spi.DispatchBridge;
import juzu.impl.common.MimeType;
import juzu.impl.common.MethodHandle;
import juzu.impl.common.Tools;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.plugin.controller.ControllerResolver;
import juzu.impl.request.Method;
import juzu.impl.bridge.spi.ScopedContext;
import juzu.impl.request.Request;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.bridge.portlet.JuzuPortlet;
import juzu.request.ApplicationContext;
import juzu.request.HttpContext;
import juzu.request.Phase;
import juzu.request.SecurityContext;
import juzu.request.UserContext;
import juzu.request.WindowContext;

import javax.portlet.BaseURL;
import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.PortletURL;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class PortletRequestBridge<Rq extends PortletRequest, Rs extends PortletResponse> implements RequestBridge {

  /** . */
  protected final Bridge bridge;

  /** . */
  protected final Rq req;

  /** . */
  protected final Rs resp;

  /** . */
  protected final Method<?> target;

  /** . */
  protected final HashMap<ControlParameter, Object> arguments;

  /** . */
  protected  final Map<String ,RequestParameter> requestParameters;

  /** . */
  protected final PortletHttpContext httpContext;

  /** . */
  protected final PortletSecurityContext securityContext;

  /** . */
  protected final PortletWindowContext windowContext;

  /** . */
  protected final PortletUserContext userContext;

  /** . */
  protected final PortletApplicationContext applicationContext;

  /** . */
  protected Request request;

  /** . */
  protected Response response;

  PortletRequestBridge(Bridge bridge, Rq req, Rs resp, PortletConfig config) {
    String methodId = null;
    Map<String, String[]> parameters = new HashMap<String, String[]>(req.getParameterMap());
    Map<String ,RequestParameter> requestParameters = Collections.emptyMap();
    for (Iterator<Map.Entry<String, String[]>> i = parameters.entrySet().iterator();i.hasNext();) {
      Map.Entry<String, String[]> parameter = i.next();
      String name = parameter.getKey();
      if (name.startsWith("juzu.")) {
        if (name.equals("juzu.op")) {
          methodId = parameter.getValue()[0];
        }
        i.remove();
      } else {
        if (requestParameters.isEmpty()) {
          requestParameters = new HashMap<String, RequestParameter>();
        }
        requestParameters.put(name, RequestParameter.create(parameter));
      }
    }

    //
    Phase phase = getPhase();
    ControllerResolver<Method> resolver = bridge.application.resolveBean(ControllerPlugin.class).getResolver();
    Method<?> target;
    if (methodId != null) {
      target = resolver.resolveMethod(phase, methodId, parameters.keySet());
    } else {
      target = resolver.resolve(phase, parameters.keySet());
    }

    // Get argument map
    HashMap<ControlParameter, Object> arguments = new HashMap<ControlParameter, Object>(target.getArguments(requestParameters));

    //
    this.bridge = bridge;
    this.req = req;
    this.resp = resp;
    this.target = target;
    this.arguments = arguments;
    this.httpContext = new PortletHttpContext(req);
    this.securityContext = new PortletSecurityContext(req);
    this.windowContext = new PortletWindowContext(this);
    this.userContext = new PortletUserContext(req);
    this.applicationContext = new PortletApplicationContext(config);
    this.requestParameters = requestParameters;
  }

  PortletRequestBridge(Bridge bridge,  Rq req, Rs resp, PortletConfig config, Method<?> target, Map<String, String[]> parameters) {

    //
    Map<String, RequestParameter> requestParameters = Collections.emptyMap();
    for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
      if (requestParameters.isEmpty()) {
        requestParameters = new HashMap<String, RequestParameter>();
      }
      RequestParameter.create(parameter).appendTo(requestParameters);
    }

    // Get argument map
    HashMap<ControlParameter, Object> arguments = new HashMap<ControlParameter, Object>(target.getArguments(requestParameters));

    //
    this.bridge = bridge;
    this.req = req;
    this.resp = resp;
    this.target = target;
    this.arguments = arguments;
    this.requestParameters = requestParameters;
    this.httpContext = new PortletHttpContext(req);
    this.securityContext = new PortletSecurityContext(req);
    this.windowContext = new PortletWindowContext(this);
    this.userContext = new PortletUserContext(req);
    this.applicationContext = new PortletApplicationContext(config);
  }

  protected abstract Phase getPhase();

  public Map<String, RequestParameter> getRequestParameters() {
    return requestParameters;
  }

  public Map<ControlParameter, Object> getArguments() {
    return arguments;
  }

  public <T> T getProperty(PropertyType<T> propertyType) {
    Object propertyValue = null;
    if (JuzuPortlet.PORTLET_MODE.equals(propertyType)) {
      propertyValue = req.getPortletMode();
    }
    else if (JuzuPortlet.WINDOW_STATE.equals(propertyType)) {
      propertyValue = req.getWindowState();
    }
    return propertyValue == null ? null : propertyType.cast(propertyValue);
  }

  public MethodHandle getTarget() {
    return target.getHandle();
  }

  public final HttpContext getHttpContext() {
    return httpContext;
  }

  public final SecurityContext getSecurityContext() {
    return securityContext;
  }

  public final WindowContext getWindowContext() {
    return windowContext;
  }

  public final UserContext getUserContext() {
    return userContext;
  }

  public final ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  public void execute(Runnable runnable) throws RejectedExecutionException {
    throw new RejectedExecutionException();
  }

  public void close() {
  }

  public final ScopedContext getScopedContext(Scope scope, boolean create) {
    ScopedContext context;
    switch (scope) {
      case REQUEST:
        context = (ScopedContext)req.getAttribute("juzu.request_scope");
        if (context == null && create) {
          req.setAttribute("juzu.request_scope", context = new ServletScopedContext(bridge.log));
        }
        break;
      case FLASH:
        PortletSession session = req.getPortletSession(create);
        if (session != null) {
          context = (ScopedContext)session.getAttribute("juzu.flash_scope");
          if (context == null && create) {
            session.setAttribute("juzu.flash_scope", context = new ServletScopedContext(bridge.log));
          }
        } else {
          context = null;
        }
        break;
      case SESSION:
        session = req.getPortletSession(create);
        if (session != null) {
          context = (ScopedContext)session.getAttribute("juzu.session_scope");
          if (context == null && create) {
            session.setAttribute("juzu.session_scope", context = new ServletScopedContext(bridge.log));
          }
        } else {
          context = null;
        }
        break;
      default:
        throw new UnsupportedOperationException("Unsupported scope " + scope);
    }
    return context;
  }

  public final void setResponse(Response response) throws IllegalArgumentException, IOException {
    this.response = response;
  }

  public void begin(Request request) {
    this.request = request;
  }

  public void end() {
    this.request = null;
  }

  public void invoke() throws Exception {
    try {
      bridge.application.resolveBean(ControllerPlugin.class).invoke(this);
    } finally {
      Tools.safeClose(this);
    }
  }

  public abstract void send() throws IOException, PortletException;

  protected void sendProperties() throws IOException {
    Iterable<Map.Entry<String, String[]>> headers = response.getProperties().getValues(PropertyType.HEADER);
    if (headers != null) {
      for (Map.Entry<String, String[]> entry : headers) {
        resp.addProperty(entry.getKey(), entry.getValue()[0]);
      }
    }
  }

  private <T> String _checkPropertyValidity(Phase phase, PropertyType<T> propertyType, T propertyValue) {
    if (propertyType == JuzuPortlet.PORTLET_MODE) {
      if (phase == Phase.RESOURCE) {
        return "Resource URL don't have portlet modes";
      }
      PortletMode portletMode = (PortletMode)propertyValue;
      for (Enumeration<PortletMode> e = req.getPortalContext().getSupportedPortletModes();e.hasMoreElements();) {
        PortletMode next = e.nextElement();
        if (next.equals(portletMode)) {
          return null;
        }
      }
      return "Unsupported portlet mode " + portletMode;
    }
    else if (propertyType == JuzuPortlet.WINDOW_STATE) {
      if (phase == Phase.RESOURCE) {
        return "Resource URL don't have windwo state";
      }
      WindowState windowState = (WindowState)propertyValue;
      for (Enumeration<WindowState> e = req.getPortalContext().getSupportedWindowStates();e.hasMoreElements();) {
        WindowState next = e.nextElement();
        if (next.equals(windowState)) {
          return null;
        }
      }
      return "Unsupported window state " + windowState;
    }
    else {
      // For now we ignore other properties
      return null;
    }
  }

  public DispatchBridge createDispatch(final Phase phase, final MethodHandle target, final Map<String, ResponseParameter> parameters) throws NullPointerException, IllegalArgumentException {
    return new DispatchBridge() {

      public MethodHandle getTarget() {
        return target;
      }

      public Map<String, ResponseParameter> getParameters() {
        return parameters;
      }

      public <T> String checkPropertyValidity(PropertyType<T> propertyType, T propertyValue) {
        return _checkPropertyValidity(phase, propertyType, propertyValue);
      }

      public void renderURL(PropertyMap properties, MimeType mimeType, Appendable appendable) throws IOException {

        if (resp instanceof MimeResponse) {
          MimeResponse mimeResp = (MimeResponse)resp;

          //
          Method method = bridge.application.resolveBean(ControllerPlugin.class).getDescriptor().getMethodByHandle(target);

          //
          BaseURL url;
          if (method.getPhase() == Phase.ACTION) {
            url = mimeResp.createActionURL();
          } else if (method.getPhase() == Phase.VIEW) {
            url = mimeResp.createRenderURL();
          } else if (method.getPhase() == Phase.RESOURCE) {
            url = mimeResp.createResourceURL();
          } else {
            throw new AssertionError();
          }

          // Set generic parameters
          for (ResponseParameter parameter : parameters.values()) {
            url.setParameter(parameter.getName(), parameter.toArray());
          }

          //
          boolean escapeXML = false;
          if (properties != null) {
            Boolean escapeXMLProperty = properties.getValue(PropertyType.ESCAPE_XML);
            if (escapeXMLProperty != null && Boolean.TRUE.equals(escapeXMLProperty)) {
              escapeXML = true;
            }

            // Handle portlet mode
            PortletMode portletModeProperty = properties.getValue(JuzuPortlet.PORTLET_MODE);
            if (portletModeProperty != null) {
              if (url instanceof PortletURL) {
                try {
                  ((PortletURL)url).setPortletMode(portletModeProperty);
                }
                catch (PortletModeException e) {
                  throw new IllegalArgumentException(e);
                }
              }
              else {
                throw new IllegalArgumentException();
              }
            }

            // Handle window state
            WindowState windowStateProperty = properties.getValue(JuzuPortlet.WINDOW_STATE);
            if (windowStateProperty != null) {
              if (url instanceof PortletURL) {
                try {
                  ((PortletURL)url).setWindowState(windowStateProperty);
                }
                catch (WindowStateException e) {
                  throw new IllegalArgumentException(e);
                }
              }
              else {
                throw new IllegalArgumentException();
              }
            }

            // Set method id
            url.setParameter("juzu.op", method.getId());
          }

          //
          if (escapeXML) {
            StringWriter writer = new StringWriter();
            url.write(writer, true);
            appendable.append(writer.toString());
          }
          else {
            appendable.append(url.toString());
          }
        } else {
          throw new IllegalStateException("Cannot render an URL during phase " + phase);
        }
      }
    };
  }
  public PortletRequest getPortletRequest() {
    return req;
  }

  public PortletResponse getPortletResponse() {
    return resp;
  }
}
