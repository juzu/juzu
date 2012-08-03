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

package juzu.impl.bridge.spi.standalone;

import juzu.PropertyType;
import juzu.Response;
import juzu.impl.asset.AssetServer;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.common.JSON;
import juzu.impl.common.MethodHandle;
import juzu.impl.common.Tools;
import juzu.impl.plugin.application.ApplicationModulePlugin;
import juzu.impl.plugin.application.metamodel.ApplicationModuleMetaModelPlugin;
import juzu.impl.plugin.controller.descriptor.MethodDescriptor;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.war.WarFileSystem;
import juzu.impl.common.Logger;
import juzu.impl.common.SimpleMap;
import juzu.impl.plugin.module.Module;
import juzu.impl.plugin.router.RouteDescriptor;
import juzu.impl.router.Param;
import juzu.impl.router.Route;
import juzu.impl.router.RouteMatch;
import juzu.impl.router.Router;
import juzu.request.Phase;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletBridge extends HttpServlet {

  /** . */
  Bridge bridge;

  /** . */
  Router router;

  /** . */
  HashMap<MethodHandle, Route> routeMap;

  /** . */
  HashMap<Route, Map<Phase, MethodHandle>> routeMap2;

  protected ClassLoader getClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

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
    ClassLoader loader = getClassLoader();

    //
    URL cfg = loader.getResource("juzu/config.json");
    if (cfg != null) {
      try {
        String s = Tools.read(cfg);
        JSON json = (JSON)JSON.parse(s);
        Module module = new Module(loader, json);
        ApplicationModulePlugin plugin = (ApplicationModulePlugin)module.getPlugin("application");
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

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
    try {
      bridge.boot();

      //
      Router router = new Router();
      HashMap<MethodHandle, Route> routeMap = new HashMap<MethodHandle, Route>();
      HashMap<Route, Map<Phase, MethodHandle>> routeMap2 = new HashMap<Route, Map<Phase, MethodHandle>>();

      //
      RouteDescriptor routesDesc = (RouteDescriptor)bridge.runtime.getDescriptor().getPlugin("router");
      if (routesDesc != null) {
        for (RouteDescriptor child : routesDesc.getChildren()) {
          Route route = router.append(child.getPath());
          for (Map.Entry<String, String> entry : child.getTargets().entrySet()) {
            MethodHandle handle = MethodHandle.parse(entry.getValue());
            Phase phase = Phase.valueOf(entry.getKey());
            routeMap.put(handle, route);
            Map<Phase, MethodHandle> map =  routeMap2.get(route);
            if (map == null) {
              routeMap2.put(route, map = new HashMap<Phase, MethodHandle>());
            }
            map.put(phase, handle);
          }
        }
      }

      //
      this.router = router;
      this.routeMap = routeMap;
      this.routeMap2 = routeMap2;
    }
    catch (Exception e) {
      throw wrap(e);
    }
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

  /** . */
  private static final Phase[] GET_PHASES = {Phase.VIEW, Phase.ACTION, Phase.RESOURCE};

  /** . */
  private static final Phase[] POST_PHASES = {Phase.ACTION, Phase.VIEW, Phase.RESOURCE};

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    //
    MethodDescriptor target = null;
    Map<String, String[]> parameters = Collections.emptyMap();

    //
    String path = req.getRequestURI().substring(req.getContextPath().length());
    if (path != null) {
      RouteMatch found = router.route(path, Collections.<String, String[]>emptyMap());
      if (found != null) {

        //
        Map<Phase, MethodHandle> m = routeMap2.get(found.getRoute());

        //
        if (m != null) {

          //
          Phase[] phases;
          if ("GET".equals(req.getMethod())) {
            phases = GET_PHASES;
          } else if ("POST".equals(req.getMethod())) {
            phases = POST_PHASES;
          } else {
            throw new UnsupportedOperationException("handle me gracefully");
          }

          //
          MethodHandle handle = null;
          for (Phase phase : phases) {
            handle = m.get(phase);
            if (handle != null) {
              break;
            }
          }

          //
          target =  bridge.runtime.getDescriptor().getControllers().getMethodByHandle(handle);
          if (found.getMatched().size() > 0 || req.getParameterMap().size() > 0) {
            parameters = new HashMap<String, String[]>();
            for (Map.Entry<String, String[]> entry : ((Map<String, String[]>)req.getParameterMap()).entrySet()) {
              parameters.put(entry.getKey(), entry.getValue().clone());
            }
            for (Map.Entry<Param, String> entry : found.getMatched().entrySet()) {
              parameters.put(entry.getKey().getName().getName(), new String[]{entry.getValue()});
            }
          }
        }
      }
    }

    //
    RequestDispatcher dispatcher = null;
    if (target == null && path != null && path.length() > 1 && !path.startsWith("/WEB-INF/")) {
      URL url = getServletContext().getResource(path);
      if (url != null) {
        dispatcher = getServletContext().getNamedDispatcher("default");
      }
    }

    //
    if (dispatcher != null) {
      dispatcher.include(req, resp);
    } else {
      if (target == null) {
        target = bridge.runtime.getDescriptor().getControllers().getResolver().resolve(Collections.<String>emptySet());
      }

      //
      ServletRequestBridge requestBridge;
      if (target != null) {
        switch (target.getPhase()) {
          case VIEW:
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

      // Implement the two phases in one
      if (requestBridge instanceof ServletActionBridge) {
        Response response = ((ServletActionBridge)requestBridge).response;
        if (response instanceof Response.Update) {
          Response.Update update = (Response.Update)response;
          Boolean redirect = response.getProperties().getValue(PropertyType.REDIRECT_AFTER_ACTION);
          if (redirect != null && !redirect) {
            requestBridge = new ServletRenderBridge(bridge.runtime.getContext(), this, req, resp, update.getTarget(), update.getParameters());
            try {
              bridge.invoke(requestBridge);
            }
            catch (Throwable throwable) {
              throw wrap(throwable);
            }
          }
        }
      }

      //
      requestBridge.send();
    }
  }
}
