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

package juzu.bridge.portlet;

import juzu.Consumes;
import juzu.PropertyType;
import juzu.impl.asset.AssetServer;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.bridge.spi.portlet.PortletEventBridge;
import juzu.impl.bridge.spi.portlet.PortletModuleContext;
import juzu.impl.fs.spi.war.WarFileSystem;
import juzu.impl.bridge.spi.portlet.PortletActionBridge;
import juzu.impl.bridge.spi.portlet.PortletRenderBridge;
import juzu.impl.bridge.spi.portlet.PortletResourceBridge;
import juzu.impl.common.Logger;
import juzu.impl.common.SimpleMap;
import juzu.impl.common.Tools;
import juzu.impl.plugin.controller.ControllerResolver;
import juzu.impl.plugin.module.Module;
import juzu.impl.plugin.module.ModuleContext;
import juzu.impl.request.Method;
import juzu.impl.resource.ResourceResolver;
import juzu.request.Phase;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventPortlet;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceServingPortlet;
import javax.portlet.WindowState;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JuzuPortlet implements Portlet, ResourceServingPortlet, EventPortlet {

  /** . */
  public static final PropertyType<PortletMode> PORTLET_MODE = new PropertyType<PortletMode>(){};

  /** . */
  public static final PropertyType<WindowState> WINDOW_STATE = new PropertyType<WindowState>(){};

  /** . */
  private Bridge bridge;

  /** . */
  private PortletContext context;

  /** . */
  private PortletConfig config;

  /** . */
  private Module module;

  public void init(final PortletConfig config) throws PortletException {
    Logger log = new Logger() {
      public void log(CharSequence msg) {
        System.out.println("[" + config.getPortletName() + "] " + msg);
      }

      public void log(CharSequence msg, Throwable t) {
        System.err.println("[" + config.getPortletName() + "] " + msg);
        t.printStackTrace();
      }
    };

    //
    AssetServer server = (AssetServer)config.getPortletContext().getAttribute("asset.server");
    if (server == null) {
      server = new AssetServer();
      config.getPortletContext().setAttribute("asset.server", server);
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
          } else if (BridgeConfig.INJECT.equals(key)) {
            // Cascade:
            // 1/ portlet init param
            // 2/ serlvet context init param
            String inject = config.getInitParameter((String)key);
            if (inject == null) {
              inject = config.getPortletContext().getInitParameter((String)key);
            }
            return inject;
          } else if (BridgeConfig.NAMES.contains(key)) {
            return config.getInitParameter((String)key);
          } else {
            return null;
          }
        }
      });
    }
    catch (Exception e) {
      String msg = "Could not find an application to start";
      if (e instanceof PortletException) {
        String nested = e.getMessage();
        if (nested != null) {
          msg += ":" + nested;
        }
        throw new PortletException(msg, e.getCause());
      } else {
        throw new PortletException(msg, e);
      }
    }

    //
    Module module = (Module)config.getPortletContext().getAttribute("juzu.module");
    if (module == null) {
      try {
        ModuleContext moduleContext = new PortletModuleContext(config.getPortletContext(), Thread.currentThread().getContextClassLoader());
        config.getPortletContext().setAttribute("juzu.module", module = new Module(moduleContext));
      }
      catch (Exception e) {
        throw new PortletException(e);
      }
    }
    module.lease();

    //
    Bridge bridge = new Bridge(
        log,
        module,
        bridgeConfig,
        WarFileSystem.create(config.getPortletContext(), "/WEB-INF/"),
        server,
        new ResourceResolver() {
          public URL resolve(String uri) {
            try {
              return context.getResource(uri);
            }
            catch (MalformedURLException e) {
              return null;
            }
          }
        });

    //
    this.config = config;
    this.bridge = bridge;
    this.context = config.getPortletContext();
    this.module = module;
  }

  /**
   * Returns the application name to use using the <code>juzu.app_name</code> init parameter of the portlet deployment
   * descriptor. Subclass can override it to provide a custom application name.
   *
   * @param config the portlet config
   * @return the application name
   */
  protected String getApplicationName(PortletConfig config) {
    return config.getInitParameter(BridgeConfig.APP_NAME);
  }

  private void rethrow(Throwable e) throws PortletException, IOException {
    if (e instanceof PortletException) {
      throw (PortletException)e;
    } else if (e instanceof IOException) {
      throw (IOException)e;
    } else {
      throw new PortletException(e);
    }
  }

  public void processAction(ActionRequest req, ActionResponse resp) throws PortletException, IOException {
    try {
      PortletActionBridge requestBridge = new PortletActionBridge(bridge, req, resp, config);
      requestBridge.invoke();
      requestBridge.send();
    }
    catch (Throwable e) {
      rethrow(e);
    }
  }

  public void processEvent(EventRequest request, EventResponse response) throws PortletException, IOException {
    ControllerResolver<Method> resolver = bridge.application.getApplication().getDescriptor().getControllers().getResolver();
    List<Method> methods = resolver.resolveMethods(Phase.EVENT, null, request.getParameterMap().keySet());

    //
    Method target = null;
    for (Method method : methods) {
      Consumes consumes = method.getMethod().getAnnotation(Consumes.class);
      if (consumes.value().equals("")) {
        target = method;
        // we don't break here on purpose because having empty match is less important
        // than an explicit match
      } else if (consumes.value().equals(request.getEvent().getName())) {
        target = method;
        break;
      }
    }

    //
    if (target != null) {
      try {
        PortletEventBridge requestBridge = new PortletEventBridge(
            bridge,
            request,
            response,
            config,
            target,
            request.getParameterMap());
        requestBridge.invoke();
        requestBridge.send();
      }
      catch (Throwable e) {
        rethrow(e);
      }
    } else {
      // We just don't dispatch however we keep the same render parameters
      response.setRenderParameters(request);
    }
  }

  private boolean initialized = false;

  public void render(final RenderRequest req, final RenderResponse resp) throws PortletException, IOException {

    //
    if (!initialized) {
      try {
        bridge.refresh();
        initialized = true;
      }
      catch (Exception e) {
        String msg = "Could not compile application";
        if (e instanceof PortletException) {
          String nested = e.getMessage();
          if (nested != null) {
            msg += ":" + nested;
          }
          throw new PortletException(msg, e.getCause());
        } else {
          throw new PortletException(msg, e);
        }
      }
    }

    //
    try {
      PortletRenderBridge requestBridge = new PortletRenderBridge(bridge, req, resp, config);
      requestBridge.invoke();
      requestBridge.send();
    }
    catch (Throwable e) {
      rethrow(e);
    }
  }

  public void serveResource(final ResourceRequest req, final ResourceResponse resp) throws PortletException, IOException {
    boolean assetRequest = "assets".equals(req.getParameter("juzu.request"));

    //
    if (assetRequest && !module.context.getRunMode().isStatic()) {
      String path = req.getResourceID();

      String contentType;
      InputStream in;
      URL url = bridge.application.getScriptManager().resolveAsset(path);
      if (url != null) {
        contentType = "text/javascript";
        in = url.openStream();
      } else {
        contentType = null;
        in = null;
      }
      if (in == null) {
        url = bridge.application.getStylesheetManager().resolveAsset(path);
        if (url != null) {
          contentType = "text/css";
          in = bridge.application.getApplication().getClassLoader().getResourceAsStream(path.substring(1));
        }
      }
      if (in != null) {
        resp.setContentType(contentType);
        Tools.copy(in, resp.getPortletOutputStream());
      } else {
        resp.addProperty(ResourceResponse.HTTP_STATUS_CODE, "404");
      }
    } else {
      try {
        PortletResourceBridge requestBridge = new PortletResourceBridge(bridge, req, resp, config);
        requestBridge.invoke();
        requestBridge.send();
      }
      catch (Throwable throwable) {
        rethrow(throwable);
      }
    }
  }

  public void destroy() {
    Tools.safeClose(bridge);
  }
}
