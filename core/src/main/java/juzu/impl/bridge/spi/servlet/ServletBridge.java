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
import juzu.impl.asset.AssetServer;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.common.DevClassLoader;
import juzu.impl.common.JSON;
import juzu.impl.common.MethodHandle;
import juzu.impl.common.QN;
import juzu.impl.common.Tools;
import juzu.impl.plugin.application.descriptor.ApplicationModuleDescriptor;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.war.WarFileSystem;
import juzu.impl.common.Logger;
import juzu.impl.common.SimpleMap;
import juzu.impl.plugin.controller.descriptor.MethodDescriptor;
import juzu.impl.plugin.module.Module;
import juzu.impl.plugin.module.ModuleLifeCycle;
import juzu.impl.resource.ResourceResolver;
import juzu.impl.router.PathParam;
import juzu.impl.router.Route;
import juzu.impl.router.RouteMatch;
import juzu.impl.router.Router;
import juzu.impl.common.URIWriter;
import juzu.request.Phase;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletBridge extends HttpServlet {

  /** . */
  private static final Phase[] GET_PHASES = {Phase.VIEW, Phase.ACTION, Phase.RESOURCE};

  /** . */
  private static final Phase[] POST_PHASES = {Phase.ACTION, Phase.VIEW, Phase.RESOURCE};

  /** . */
  Router root;

  /** . */
  List<Handler> handlers;

  /** . */
  Handler defaultHandler;

  /** . */
  ModuleLifeCycle moduleLifeCycle;

  /** . */
  AssetServer server;

  /** . */
  WarFileSystem resources;

  /** . */
  Logger log;

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
    String srcPath = config.getInitParameter("juzu.src_path");
    ReadFileSystem<?> sourcePath = srcPath != null ? new DiskFileSystem(new File(srcPath)) : WarFileSystem.create(config.getServletContext(), "/WEB-INF/src/");
    WarFileSystem resources = WarFileSystem.create(config.getServletContext(), "/WEB-INF/");

    //
    int runMode = BridgeConfig.getRunMode(new SimpleMap<String, String>() {
      @Override
      protected Iterator<String> keys() {
        return Tools.iterator(BridgeConfig.RUN_MODE);
      }
      @Override
      public String get(Object key) {
        return key.equals(BridgeConfig.RUN_MODE) ? config.getInitParameter(BridgeConfig.RUN_MODE) : null;
      }
    });

    // Build module
    ModuleLifeCycle moduleLifeCycle;
    switch (runMode) {
      case BridgeConfig.DYNAMIC_MODE:
        moduleLifeCycle = new ModuleLifeCycle.Dynamic(log, new DevClassLoader(Thread.currentThread().getContextClassLoader()), sourcePath);
        break;
      default:
        moduleLifeCycle = new ModuleLifeCycle.Static(log, Thread.currentThread().getContextClassLoader(), WarFileSystem.create(config.getServletContext(), "/WEB-INF/classes/"));
    }

    //
    this.moduleLifeCycle = moduleLifeCycle;
    this.server = server;
    this.resources = resources;
    this.log = log;
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
    try {
      boolean stale = moduleLifeCycle.refresh();
      if (stale) {
        this.root = null;
        this.handlers = null;
        this.defaultHandler = null;
      }
    }
    catch (Exception e) {
      throw wrap(e);
    }

    //
    if (root == null) {

      // Create module
      Module module;
      try {
        URL cfg = moduleLifeCycle.getClassLoader().getResource("juzu/config.json");
        String s = Tools.read(cfg);
        JSON json = (JSON)JSON.parse(s);
        module = new Module(moduleLifeCycle.getClassLoader(), json);
      }
      catch (Exception e) {
        throw wrap(e);
      }

      // Get application descriptor from module
      ApplicationModuleDescriptor desc = (ApplicationModuleDescriptor)module.getDescriptors().get("application");

      // Build all applications
      Map<String, Bridge> applications = new HashMap<String, Bridge>();
      for (final QN name : desc.getNames()) {
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
                return name.toString();
              } else if (BridgeConfig.NAMES.contains(key)) {
                return getServletConfig().getInitParameter((String)key);
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

        // Create and configure bridge
        Bridge bridge = new Bridge(moduleLifeCycle);
        bridge.config = bridgeConfig;
        bridge.resources = resources;
        bridge.server = server;
        bridge.log = log;
        bridge.resolver = new ResourceResolver() {
          public URL resolve(String uri) {
            try {
              return getServletConfig().getServletContext().getResource(uri);
            }
            catch (MalformedURLException e) {
              return null;
            }
          }
        };

        //
        applications.put(name.toString(), bridge);
      }

      //
      String applicationName = getApplicationName(getServletConfig());

      // Build first mounted applications
      LinkedHashMap<String, Handler> handlers = new LinkedHashMap<String, Handler>();
      Handler defaultHandler = null;
      Router root = new Router();
      for (Map.Entry<String, Bridge> entry : applications.entrySet()) {
        Handler handler = new Handler(root, entry.getValue());
        if (entry.getKey().equals(applicationName)) {
          defaultHandler = handler;
        }
        handlers.put(entry.getKey(), handler);
      }

      //
      this.root = root;
      this.handlers = new ArrayList<Handler>(handlers.values());
      this.defaultHandler = defaultHandler;
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    //
    refresh();

    //
    Handler targetHandler = null;
    MethodDescriptor target = null;
    Map<String, String[]> parameters = Collections.emptyMap();

    //
    String path = req.getRequestURI().substring(req.getContextPath().length());
    RouteMatch match;
    if (path != null) {
      match = root.route(path, Collections.<String, String[]>emptyMap());
      if (match != null) {
        for (Handler handler : handlers) {
          if (handler.routes.contains(match.getRoute())) {
            targetHandler = handler;
            Map<Phase, MethodHandle> m = handler.routeMap2.get(match.getRoute());
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
                  target =  targetHandler.bridge.runtime.getDescriptor().getControllers().getMethodByHandle(handle);
                  if (match.getMatched().size() > 0 || req.getParameterMap().size() > 0) {
                    parameters = new HashMap<String, String[]>();
                    for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
                      parameters.put(entry.getKey(), entry.getValue().clone());
                    }
                    for (Map.Entry<PathParam, String> entry : match.getMatched().entrySet()) {
                      parameters.put(entry.getKey().getName(), new String[]{entry.getValue()});
                    }
                  }
                  break;
                }
              }
            }
          }
        }
      }
      if (targetHandler == null) {
        targetHandler = defaultHandler;
      }
    } else {
      targetHandler = defaultHandler;
      match = null;
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

      //
      if (target == null) {
        if (targetHandler != null) {
          target = targetHandler.bridge.runtime.getDescriptor().getControllers().getResolver().resolve(Collections.<String>emptySet());
          if (target != null) {
            Route route = targetHandler.routeMap.get(target.getHandle());
            if (route == null && match == null) {
              route = targetHandler.root;
            }
            if (route != null) {
              Map<String, String> empty = Collections.emptyMap();
              match = route.matches(empty);
              if (match != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(req.getScheme());
                sb.append("://");
                sb.append(req.getServerName());
                int port = req.getServerPort();
                if (port != 80) {
                  sb.append(':').append(Integer.toString(port));
                }
                sb.append(req.getContextPath());
                match.render(new URIWriter(sb));
                resp.sendRedirect(sb.toString());
                return;
              } else {
                // ?
              }
            } else {
              //
            }
          } else {
            resp.sendError(404);
            return;
          }
        } else {
          resp.sendError(404);
          return;
        }
      }

      //
      ServletRequestBridge requestBridge;
      if (target.getPhase() == Phase.ACTION) {
        requestBridge = new ServletActionBridge(targetHandler.bridge.runtime.getContext(), targetHandler, req, resp, target.getHandle(), parameters);
      } else if (target.getPhase() == Phase.VIEW) {
        requestBridge = new ServletRenderBridge(targetHandler.bridge.runtime.getContext(), targetHandler, req, resp, target.getHandle(), parameters);
      } else if (target.getPhase() == Phase.RESOURCE) {
        requestBridge = new ServletResourceBridge(targetHandler.bridge.runtime.getContext(), targetHandler, req, resp, target.getHandle(), parameters);
      } else {
        throw new ServletException("Cannot decode phase");
      }

      //
      try {
        targetHandler.bridge.invoke(requestBridge);
      }
      catch (Throwable throwable) {
        throw ServletBridge.wrap(throwable);
      }

      // Implement the two phases in one
      if (requestBridge instanceof ServletActionBridge) {
        Response response = ((ServletActionBridge)requestBridge).response;
        if (response instanceof Response.Update) {
          Response.Update update = (Response.Update)response;
          Boolean redirect = response.getProperties().getValue(PropertyType.REDIRECT_AFTER_ACTION);
          if (redirect != null && !redirect) {
            requestBridge = new ServletRenderBridge(targetHandler.bridge.runtime.getContext(), targetHandler, req, resp, update.getTarget(), update.getParameters());
            try {
              targetHandler.bridge.invoke(requestBridge);
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
}
