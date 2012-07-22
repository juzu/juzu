/*
 * Copyright (C) 2011 eXo Platform SAS.
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

import juzu.Response;
import juzu.impl.asset.AssetServer;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.common.MethodHandle;
import juzu.impl.common.QualifiedName;
import juzu.impl.compiler.CompilationError;
import juzu.impl.controller.descriptor.ControllersDescriptor;
import juzu.impl.controller.descriptor.MethodDescriptor;
import juzu.impl.controller.descriptor.RouteDescriptor;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.war.WarFileSystem;
import juzu.impl.common.Logger;
import juzu.impl.common.SimpleMap;
import juzu.impl.router.Param;
import juzu.impl.router.Route;
import juzu.impl.router.RouteMatch;
import juzu.impl.router.Router;
import juzu.standalone.JuzuServlet;

import javax.portlet.ActionRequest;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletBridge extends HttpServlet {

  /** . */
  static final QualifiedName TARGET = QualifiedName.create("jz", "target");

  /** . */
  Bridge bridge;

  /** . */
  Router router;

  /** . */
  HashMap<MethodHandle, Route> routeMap;

  /** . */
  HashMap<Route, MethodHandle> routeMap2;

  @Override
  public void init() throws ServletException {

    //
    final ServletConfig config = getServletConfig();

    //
    Logger log = new Logger() {
      public void log(CharSequence msg) {
        System.out.println("[" + config.getServletName() + "] " + msg);
      }

      public void log(CharSequence msg, Throwable t) {
        System.err.println("[" + config.getServletName() + "] " + msg);
        t.printStackTrace();
      }
    };

    //
    AssetServer server = (AssetServer)config.getServletContext().getAttribute("asset.server");
    if (server == null) {
      server = new AssetServer();
      config.getServletContext().setAttribute("asset.server", server);
    }

    //
    BridgeConfig bridgeConfig;
    try {
      bridgeConfig = new BridgeConfig(new SimpleMap<String, String>() {
        @Override
        protected Iterator<String> keys() {
          return BridgeConfig.NAMES.iterator();
        }

        @Override
        public String get(Object key) {
          if (BridgeConfig.APP_NAME.equals(key)) {
            return getApplicationName(config);
          } else if (BridgeConfig.NAMES.contains(key)) {
            return config.getInitParameter((String)key);
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
    String srcPath = config.getInitParameter("juzu.src_path");
    ReadFileSystem<?> sourcePath = srcPath != null ? new DiskFileSystem(new File(srcPath)) : WarFileSystem.create(config.getServletContext(), "/WEB-INF/src/");

    //
    Bridge bridge = new Bridge();
    bridge.config = bridgeConfig;
    bridge.resources = WarFileSystem.create(config.getServletContext(), "/WEB-INF/");
    bridge.server = server;
    bridge.log = log;
    bridge.sourcePath = sourcePath;
    bridge.classes = WarFileSystem.create(config.getServletContext(), "/WEB-INF/classes/");

    //
    this.bridge = bridge;

    //
    Collection<CompilationError> errors;
    try {
      errors = bridge.boot();
    }
    catch (Exception e) {
      throw wrap(e);
    }
    if (errors != null && errors.size() > 0) {
      log.log("Error when compiling application " + errors);
    }

    //
    Router router = new Router();
    HashMap<MethodHandle, Route> routeMap = new HashMap<MethodHandle, Route>();
    HashMap<Route, MethodHandle> routeMap2 = new HashMap<Route, MethodHandle>();
    ControllersDescriptor desc = bridge.runtime.getDescriptor().getControllers();
    for (RouteDescriptor routeDesc : desc.getRoutes()) {
      Route route = router.append(routeDesc.getPath());
      routeMap.put(routeDesc.getTarget(), route);
      routeMap2.put(route, routeDesc.getTarget());
    }
    this.router = router;
    this.routeMap = routeMap;
    this.routeMap2 = routeMap2;
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

  private ServletException wrap(Throwable e) {
    return e instanceof ServletException ? (ServletException)e : new ServletException("Could not find an application to start", e);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doGet(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    //
    MethodDescriptor target = null;
    Map<String, String[]> parameters = Collections.emptyMap();

    //
    String path = req.getRequestURI().substring(req.getContextPath().length());
    if (path != null) {
      RouteMatch match = router.route(path);
      if (match != null) {
        MethodHandle handle = routeMap2.get(match.getRoute());
        target =  bridge.runtime.getDescriptor().getControllers().getMethodByHandle(handle);
        if (match.getMatched().size() > 0 || req.getParameterMap().size() > 0) {
          parameters = new HashMap<String, String[]>();
          for (Map.Entry<String, String[]> entry : ((Map<String, String[]>)req.getParameterMap()).entrySet()) {
            parameters.put(entry.getKey(), entry.getValue().clone());
          }
          for (Map.Entry<Param, String> entry : match.getMatched().entrySet()) {
            parameters.put(entry.getKey().getName().getName(), new String[]{entry.getValue()});
          }
        }
      }
    }

    //
    ServletRequestBridge requestBridge;
    if (target != null) {
      switch (target.getPhase()) {
        case RENDER:
          requestBridge = new ServletRenderBridge(bridge.runtime.getContext(), this, req, resp, target.getHandle(), parameters);
          break;
        case ACTION: {
          requestBridge = new ServletActionBridge(bridge.runtime.getContext(), this, req, resp, target.getHandle(), parameters);
          break;
        }
        case RESOURCE:
          requestBridge = new ServletResourceBridge(bridge.runtime.getContext(), this, req, resp, target.getHandle(), parameters);
          break;
        default:
          throw new ServletException("Cannot decode phase");
      }
    } else {
      requestBridge = new ServletRenderBridge(bridge.runtime.getContext(), this, req, resp, null, parameters);
    }

    //
    try {
      bridge.invoke(requestBridge);
    }
    catch (Throwable throwable) {
      throw wrap(throwable);
    }

    //
    if (requestBridge instanceof ServletActionBridge) {
      Response response = ((ServletActionBridge)requestBridge).response;
      if (response instanceof Response.Update) {
        Response.Update update = (Response.Update)response;
        Boolean redirect = response.getProperties().getValue(JuzuServlet.REDIRECT_AFTER_ACTION);
        if (redirect == null || redirect) {
          String url = requestBridge.renderURL(update.getTarget(), update.getParameters(), update.getProperties());
          for (Map.Entry<String, String[]> entry : requestBridge.responseHeaders.entrySet()) {
            resp.setHeader(entry.getKey(), entry.getValue()[0]);
          }
          resp.sendRedirect(url);
        } else {
          ServletRenderBridge renderBridge = new ServletRenderBridge(bridge.runtime.getContext(), this, req, resp, update.getTarget(), update.getParameters());
          try {
            bridge.invoke(renderBridge);
          }
          catch (Throwable throwable) {
            throw wrap(throwable);
          }
        }
      }
      else if (response instanceof Response.Redirect) {
        Response.Redirect redirect = (Response.Redirect)response;
        String url = redirect.getLocation();
        resp.sendRedirect(url);
      }
    }
  }
}
