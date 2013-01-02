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

import juzu.PropertyType;
import juzu.Response;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.common.MethodHandle;
import juzu.impl.common.Tools;
import juzu.impl.plugin.application.descriptor.ApplicationModuleDescriptor;
import juzu.impl.common.Logger;
import juzu.impl.common.SimpleMap;
import juzu.impl.request.Method;
import juzu.impl.resource.ResourceResolver;
import juzu.impl.router.PathParam;
import juzu.impl.router.Route;
import juzu.impl.router.RouteMatch;
import juzu.impl.common.URIWriter;
import juzu.request.Phase;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletBridge extends HttpServlet {

  /** . */
  private static final Phase[] GET_PHASES = {Phase.VIEW, Phase.ACTION, Phase.RESOURCE};

  /** . */
  private static final Phase[] POST_PHASES = {Phase.ACTION, Phase.VIEW, Phase.RESOURCE};

  /** . */
  ServletModule module;

  /** . */
  Logger log;

  /** . */
  private BridgeConfig config;

  /** . */
  private Handler handler;

  @Override
  public void init() throws ServletException {

    //
    final ServletConfig servletConfig = getServletConfig();

    //
    Logger log = new Logger() {
      public void log(CharSequence msg) {
        System.out.println("[" + servletConfig.getServletName() + "] " + msg);
      }

      public void log(CharSequence msg, Throwable t) {
        System.err.println("[" + servletConfig.getServletName() + "] " + msg);
        t.printStackTrace();
      }
    };

    //
    BridgeConfig config;
    try {
      config = new BridgeConfig(new SimpleMap<String, String>() {
        @Override
        protected Iterator<String> keys() {
          return BridgeConfig.NAMES.iterator();
        }
        @Override
        public String get(Object key) {
          if (BridgeConfig.APP_NAME.equals(key)) {
            return getApplicationName(servletConfig);
          } else if (BridgeConfig.INJECT.equals(key)) {
            // Cascade:
            // 1/ portlet init param
            // 2/ serlvet context init param
            String inject = servletConfig.getInitParameter((String)key);
            if (inject == null) {
              inject = servletConfig.getServletContext().getInitParameter((String)key);
            }
            return inject;
          } else if (BridgeConfig.NAMES.contains(key)) {
            return servletConfig.getInitParameter((String)key);
          } else {
            return null;
          }
        }
      });
    }
    catch (Exception e) {
      throw wrap(e);
    }

    //
    if (config.name == null) {
      throw new ServletException("No application configured");
    }

    //
    this.module = null;
    this.log = log;
    this.config = config;
    this.handler = null;
  }

  static ServletException wrap(Throwable e) {
    return e instanceof ServletException ? (ServletException)e : new ServletException("Could not find an application to start", e);
  }

  /**
   * Returns the application name to use using the <code>juzu.app_name</code> init parameter of the portlet deployment
   * descriptor. Subclass can override it to provide a custom application name.
   *
   * @param config the portlet config
   * @return the application name
   */
  protected String getApplicationName(ServletConfig config) {
    return config.getInitParameter("juzu.app_name");
  }

  private void refresh() throws ServletException {

    //
    if (module == null) {
      try {
        module = ServletModule.leaseModule(getServletContext());
      }
      catch (Exception e) {
        throw wrap(e);
      }
    }

    //
    try {
      boolean stale = module.lifeCycle.refresh();
      if (stale) {
        if (handler != null) {
          Tools.safeClose(handler);
          handler = null;
        }
      }
    }
    catch (Exception e) {
      throw wrap(e);
    }

    //
    if (handler == null) {

      // Get application descriptor from module
      ApplicationModuleDescriptor desc = (ApplicationModuleDescriptor)module.getDescriptors().get("application");

      // Build application

      // Create and configure bridge
      Bridge bridge = new Bridge(
          log,
          this.module.lifeCycle,
          this.config,
          this.module.resources,
          this.module.server,
          new ResourceResolver() {
        public URL resolve(String uri) {
          try {
            return getServletConfig().getServletContext().getResource(uri);
          }
          catch (MalformedURLException e) {
            return null;
          }
        }
      });

      //
      String path = null;
      ServletRegistration reg = getServletContext().getServletRegistration(getServletName());
      for (String mapping : reg.getMappings()) {
        if ("/".equals(mapping)) {
          path = "";
          break;
        } else if ("/*".equals(mapping)) {
          throw new UnsupportedOperationException("Implement me");
        } else if (mapping.endsWith("/*")) {
          path = mapping.substring(0, mapping.length() - 2);
        } else {
          throw new UnsupportedOperationException("Should not be possible");
        }
      }

      //
      if (path == null) {
        throw new ServletException("Juzu servlet should be mounted on an url pattern");
      }

      //
      this.handler = new Handler(bridge, path);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    //
    refresh();

    //
    String path = req.getRequestURI().substring(req.getContextPath().length());

    // Determine first a possible match from the root route from the request path
    RouteMatch requestMatch = null;
    if (path.startsWith(handler.path)) {
      requestMatch = handler.root.route(path.substring(handler.path.length()), Collections.<String, String[]>emptyMap());
    }

    // Determine a method + parameters if we have a match
    Method requestMethod = null;
    Map<String, String[]> requestParameters = Collections.emptyMap();
    if (requestMatch != null) {
      Map<Phase, MethodHandle> m = handler.backwardRoutes.get(requestMatch.getRoute());
      if (m != null) {
        Phase[] phases;
        if ("GET".equals(req.getMethod())) {
          phases = GET_PHASES;
        } else if ("POST".equals(req.getMethod())) {
          phases = POST_PHASES;
        } else {
          throw new UnsupportedOperationException("handle me gracefully");
        }
        for (Phase phase : phases) {
          MethodHandle handle = m.get(phase);
          if (handle != null) {
            requestMethod =  handler.bridge.application.getDescriptor().getControllers().getMethodByHandle(handle);
            if (requestMatch.getMatched().size() > 0 || req.getParameterMap().size() > 0) {
              requestParameters = new HashMap<String, String[]>();
              for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
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

      // Do we need to send a server resource ?
      if (path != null && path.length() > 1 && !path.startsWith("/WEB-INF/")) {
        URL url = getServletContext().getResource(path);
        if (url != null) {
          RequestDispatcher dispatcher = getServletContext().getNamedDispatcher("default");
          dispatcher.include(req, resp);
          return;
        }
      }

      // If we have an handler we locate the index method
      if (handler != null) {
        requestMethod = handler.bridge.application.getDescriptor().getControllers().getResolver().resolve(Phase.VIEW, Collections.<String>emptySet());
      }
    }

    // No method -> not found
    if (requestMethod == null) {
      resp.sendError(404);
    } else {
      if (requestMatch == null) {
        Route requestRoute = handler.forwardRoutes.get(requestMethod.getHandle());
        if (requestRoute != null) {
          requestMatch = requestRoute.matches(Collections.<String, String>emptyMap());
          if (requestMatch != null) {
            StringBuilder sb = new StringBuilder();
            requestMatch.render(new URIWriter(sb));
            if (!sb.toString().equals(path)) {
              String redirect =
                  req.getScheme() + "://" + req.getServerName() + (req.getServerPort() != 80 ? (":" + req.getServerPort()) : "") +
                      req.getContextPath() +
                      handler.path +
                      sb;
              resp.sendRedirect(redirect);
              return;
            }
          }
        }
      }

      //
      ServletRequestBridge requestBridge;
      if (requestMethod.getPhase() == Phase.ACTION) {
        requestBridge = new ServletActionBridge(handler.bridge.application.getApplication(), handler, req, resp, requestMethod, requestParameters);
      } else if (requestMethod.getPhase() == Phase.VIEW) {
        requestBridge = new ServletRenderBridge(handler.bridge.application.getApplication(), handler, req, resp, requestMethod, requestParameters);
      } else if (requestMethod.getPhase() == Phase.RESOURCE) {
        requestBridge = new ServletResourceBridge(handler.bridge.application.getApplication(), handler, req, resp, requestMethod, requestParameters);
      } else {
        throw new ServletException("Cannot decode phase");
      }

      //
      try {
        handler.bridge.invoke(requestBridge);
      }
      catch (Throwable throwable) {
        throw ServletBridge.wrap(throwable);
      }

      // Implement the two phases in one
      if (requestBridge instanceof ServletActionBridge) {
        Response response = ((ServletActionBridge)requestBridge).response;
        if (response instanceof Response.View) {
          Response.View update = (Response.View)response;
          Boolean redirect = response.getProperties().getValue(PropertyType.REDIRECT_AFTER_ACTION);
          if (redirect != null && !redirect) {
            Method<?> desc = handler.bridge.application.getDescriptor().getControllers().getMethodByHandle(update.getTarget());
            requestBridge = new ServletRenderBridge(handler.bridge.application.getApplication(), handler, req, resp, desc, update.getParameters());
            try {
              handler.bridge.invoke(requestBridge);
            }
            catch (Throwable throwable) {
              throw ServletBridge.wrap(throwable);
            }
          }
        }
      }

      //
      requestBridge.send();
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doGet(req, resp);
  }

  @Override
  public void destroy() {
    if (module != null) {
      module.release();
    }
    if (handler != null) {
      Tools.safeClose(handler);
      this.handler = null;
    }
  }
}
