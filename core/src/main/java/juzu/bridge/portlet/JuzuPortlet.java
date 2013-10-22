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

package juzu.bridge.portlet;

import juzu.Consumes;
import juzu.PropertyType;
import juzu.impl.asset.AssetServer;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.bridge.BridgeContext;
import juzu.impl.bridge.module.ApplicationBridge;
import juzu.impl.bridge.spi.portlet.PortletEventBridge;
import juzu.impl.common.JUL;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.war.WarFileSystem;
import juzu.impl.bridge.spi.portlet.PortletActionBridge;
import juzu.impl.bridge.spi.portlet.PortletRenderBridge;
import juzu.impl.bridge.spi.portlet.PortletResourceBridge;
import juzu.impl.common.Logger;
import juzu.impl.common.SimpleMap;
import juzu.impl.common.Tools;
import juzu.impl.inject.spi.Injector;
import juzu.impl.inject.spi.spring.SpringInjector;
import juzu.impl.plugin.asset.AssetPlugin;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.plugin.controller.ControllerResolver;
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
import java.io.File;
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

  public void init(final PortletConfig config) throws PortletException {
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
    final BridgeContext bridgeContext = new BridgeContext() {
      final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      final ResourceResolver resolver = new ResourceResolver() {
        public URL resolve(String uri) {
          try {
            return context.getResource(uri);
          }
          catch (MalformedURLException e) {
            return null;
          }
        }
      };
      public ReadFileSystem<?> getResourcePath() {
        return WarFileSystem.create(context, "/WEB-INF/");
      }
      public ReadFileSystem<?> getSourcePath() {
        String srcPath = context.getInitParameter(BridgeConfig.SOURCE_PATH);
        return srcPath != null ? new DiskFileSystem(new File(srcPath)) : WarFileSystem.create(context, "/WEB-INF/src/");
      }
      public ReadFileSystem<?> getClassPath() {
        return WarFileSystem.create(context, "/WEB-INF/classes/");
      }
      public ClassLoader getClassLoader() {
        return classLoader;
      }
      public String getInitParameter(String name) {
        return context.getInitParameter(name);
      }
      public ResourceResolver getResolver() {
        return resolver;
      }
      public Object getAttribute(String key) {
        return context.getAttribute(key);
      }
      public void setAttribute(String key, Object value) {
        context.setAttribute(key, value);
      }
      public Logger getLogger(String name) {
        return JUL.getLogger(name);
      }
    };

    //
    Injector injector = bridgeConfig.injectorProvider.get();
    if (injector instanceof SpringInjector) {
      SpringInjector springInjector = (SpringInjector)injector;
      Object parent = config.getPortletContext().getAttribute("org.springframework.web.context.WebApplicationContext.ROOT");
      if (parent != null) {
        springInjector.setParent(parent);
      }
    }

    //
    Bridge bridge = new ApplicationBridge(
        bridgeContext,
        bridgeConfig,
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
        },
        injector);

    //
    this.config = config;
    this.bridge = bridge;
    this.context = config.getPortletContext();
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
    ControllerResolver<Method> resolver = bridge.getApplication().resolveBean(ControllerPlugin.class).getDescriptor().getResolver();
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

    // THIS CODE SHOULD BE REMOVED
    // BECAUSE IT DOES NOT BEHAVE CORRECTLY WHEN USED IN PRETTY FAIL MODE
    // AND IT IS ALSO HANDLED IN THE REQUEST BRIDGE
    if (!initialized) {
      try {
        bridge.refresh();
        initialized = true;
      }
      catch (Exception e) {
        String msg = "Could not initialize application";
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

    // THIS CODE SHOULD BE REMOVED
    // BECAUSE IT DOES NOT BEHAVE CORRECTLY WHEN USED IN PRETTY FAIL MODE
    // AND IT IS ALSO HANDLED IN THE REQUEST BRIDGE
    if (!initialized) {
      try {
        bridge.refresh();
        initialized = true;
      }
      catch (Exception e) {
        String msg = "Could not initialize application";
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
    boolean assetRequest = "assets".equals(req.getParameter("juzu.request"));

    //
    if (assetRequest && !bridge.getRunMode().isStatic()) {
      String path = req.getResourceID();
      AssetPlugin assetPlugin = (AssetPlugin)bridge.getApplication().getPlugin("asset");
      String contentType;
      InputStream in;
      URL url = assetPlugin.getAssetManager().resolveAsset(path);
      if (url != null) {
        if (path.endsWith(".css")) {
          contentType = "text/css";
        } else if (path.endsWith(".js")) {
          contentType = "text/javascript";
        } else {
          contentType = null;
        }
        in = url.openStream();
      } else {
        contentType = null;
        in = null;
      }
      if (in != null) {
        if (contentType != null) {
          resp.setContentType(contentType);
        }
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
