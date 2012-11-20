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
import juzu.impl.common.QN;
import juzu.impl.common.QualifiedName;
import juzu.impl.common.Tools;
import juzu.impl.plugin.application.descriptor.ApplicationModuleDescriptor;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.war.WarFileSystem;
import juzu.impl.common.Logger;
import juzu.impl.common.SimpleMap;
import juzu.impl.plugin.controller.descriptor.MethodDescriptor;
import juzu.impl.plugin.module.Module;
import juzu.impl.plugin.router.RouteDescriptor;
import juzu.impl.resource.ResourceResolver;
import juzu.impl.router.PathParam;
import juzu.impl.router.Route;
import juzu.impl.router.RouteMatch;
import juzu.impl.router.Router;
import juzu.impl.router.URIWriter;
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
    AssetServer server = (AssetServer)config.getServletContext().getAttribute("asset.server");
    if (server == null) {
      server = new AssetServer();
      config.getServletContext().setAttribute("asset.server", server);
    }

    //
    String srcPath = config.getInitParameter("juzu.src_path");
    ReadFileSystem<?> sourcePath = srcPath != null ? new DiskFileSystem(new File(srcPath)) : WarFileSystem.create(config.getServletContext(), "/WEB-INF/src/");
    WarFileSystem classes = WarFileSystem.create(config.getServletContext(), "/WEB-INF/classes/");
    WarFileSystem resources = WarFileSystem.create(config.getServletContext(), "/WEB-INF/");

    //
    Module module;
    URL cfg = loader.getResource("juzu/config.json");
    try {
      String s = Tools.read(cfg);
      JSON json = (JSON)JSON.parse(s);
      module = new Module(loader, json);
    }
    catch (Exception e) {
      throw new ServletException(e);
    }

    //
    ApplicationModuleDescriptor desc = (ApplicationModuleDescriptor)module.getDescriptors().get("application");

    //
    Map<String, Bridge> applications = new HashMap<String, Bridge>();
    for (final QN name : desc.getNames()) {


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
              return name.toString();
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

      // Create and configure bridge
      Bridge bridge = new Bridge();
      bridge.config = bridgeConfig;
      bridge.resources = resources;
      bridge.server = server;
      bridge.log = log;
      bridge.sourcePath = sourcePath;
      bridge.classes = classes;
      bridge.resolver = new ResourceResolver() {
        public URL resolve(String uri) {
          try {
            return config.getServletContext().getResource(uri);
          }
          catch (MalformedURLException e) {
            return null;
          }
        }
      };

      //
      applications.put(name.toString(), bridge);
    }

    // Build first mounted applications
    RouteDescriptor routesDesc = (RouteDescriptor)module.getDescriptors().get("router");
    LinkedHashMap<String, Handler> handlers = new LinkedHashMap<String, Handler>();
    if (routesDesc != null) {
      Router root = new Router();
      for (RouteDescriptor child : routesDesc.getChildren()) {
        Route route = root.append(child.getPath());
        String application = child.getTargets().get("application");
        Bridge bridge = applications.get(application);
        handlers.put(application, new Handler(route, bridge));
      }
      this.root = root;
    } else {
      this.root = null;
    }

    //
    this.handlers = new ArrayList<Handler>(handlers.values());

    //
    String applicationName = getApplicationName(config);
    if (applicationName != null) {
      Handler defaultHandler = handlers.get(applicationName);
      if (defaultHandler == null) {
        defaultHandler = new Handler(new Router(), applications.get(applicationName));
      }
      this.defaultHandler = defaultHandler;
    } else {
      this.defaultHandler = null;
    }
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

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


    Handler targetHandler = null;
    MethodDescriptor target = null;
    Map<String, String[]> parameters = Collections.emptyMap();

    //
    String path = req.getRequestURI().substring(req.getContextPath().length());
    if (path != null) {

      //
      Route abc;
      List<Handler> def;
      if (root != null) {
        abc = root;
        def = handlers;
      } else if (defaultHandler != null) {
        abc = defaultHandler.root;
        def = Collections.singletonList(defaultHandler);
      } else {
        abc = null;
        def = null;
      }

      //
      RouteMatch found;
      if (abc != null) {
        found = abc.route(path, Collections.<String, String[]>emptyMap());
      } else {
        found = null;
      }

      //
      if (found != null) {

        //
        Map<Phase, MethodHandle> m = null;
        for (Handler handler : def) {
          if (handler.root == found.getRoute()) {
            targetHandler = handler;
            break;
          } else {
            m = handler.routeMap2.get(found.getRoute());
            if (m != null) {
              targetHandler = handler;
              break;
            }
          }
        }

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
          target =  targetHandler.bridge.runtime.getDescriptor().getControllers().getMethodByHandle(handle);
          if (found.getMatched().size() > 0 || req.getParameterMap().size() > 0) {
            parameters = new HashMap<String, String[]>();
            for (Map.Entry<String, String[]> entry : ((Map<String, String[]>)req.getParameterMap()).entrySet()) {
              parameters.put(entry.getKey(), entry.getValue().clone());
            }
            for (Map.Entry<PathParam, String> entry : found.getMatched().entrySet()) {
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

        //
        if (targetHandler == null) {
          if (defaultHandler == null) {
            resp.sendError(404);
            return;
          } else {
            if (defaultHandler.root.getParent() == null) {
              targetHandler = defaultHandler;
            } else {
              Map<QualifiedName, String> empty = Collections.emptyMap();
              RouteMatch match = defaultHandler.root.matches(empty);
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
                resp.sendError(404);
                return;
              }
            }
          }
        }

        //
        target = targetHandler.bridge.runtime.getDescriptor().getControllers().getResolver().resolve(Collections.<String>emptySet());
      }

      //
      ServletRequestBridge requestBridge;
      if (target != null) {
        switch (target.getPhase()) {
          case VIEW:
            requestBridge = new ServletRenderBridge(targetHandler.bridge.runtime.getContext(), targetHandler, req, resp, target.getHandle(), parameters);
            break;
          case ACTION: {
            requestBridge = new ServletActionBridge(targetHandler.bridge.runtime.getContext(), targetHandler, req, resp, target.getHandle(), parameters);
            break;
          }
          case RESOURCE:
            requestBridge = new ServletResourceBridge(targetHandler.bridge.runtime.getContext(), targetHandler, req, resp, target.getHandle(), parameters);
            break;
          default:
            throw new ServletException("Cannot decode phase");
        }
      } else {
        requestBridge = new ServletRenderBridge(targetHandler.bridge.runtime.getContext(), targetHandler, req, resp, null, parameters);
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
