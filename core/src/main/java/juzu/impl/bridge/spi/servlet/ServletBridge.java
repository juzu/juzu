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

package juzu.impl.bridge.spi.servlet;

import juzu.impl.asset.AssetServer;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.bridge.BridgeContext;
import juzu.impl.bridge.module.ApplicationBridge;
import juzu.impl.bridge.provided.ProvidedBridge;
import juzu.impl.bridge.spi.web.Handler;
import juzu.impl.common.Completion;
import juzu.impl.common.JUL;
import juzu.impl.common.Tools;
import juzu.impl.common.Logger;
import juzu.impl.common.SimpleMap;
import juzu.impl.compiler.CompilationException;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.war.WarFileSystem;
import juzu.impl.inject.spi.Injector;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.inject.spi.spring.SpringInjector;
import juzu.impl.resource.ResourceResolver;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletBridge extends HttpServlet {

  /** The resource bundle name. */
  public static final String BUNDLE_NAME = "juzu.resource_bundle";

  /** . */
  private String path;

  /** . */
  private BridgeConfig config;

  /** . */
  private Bridge bridge;

  /** . */
  private Handler handler;

  /** . */
  private String bundleName;

  /** . */
  ServletApplicationContext applicationContext;

  Bridge getBridge() {
    return bridge;
  }

  @Override
  public void init() throws ServletException {

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
    if (path == null) {
      throw new ServletException("Juzu servlet should be mounted on an url pattern");
    }

    //
    Logger servletLogger = JUL.getLogger(ServletBridge.class + "." + getServletConfig().getServletName());

    //
    final ServletConfig servletConfig = getServletConfig();

    //
    BridgeConfig config;
    try {
      config = new BridgeConfig(servletLogger, new SimpleMap<String, String>() {
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
          } else if (BridgeConfig.REQUEST_ENCODING.equals(key)) {
            return servletConfig.getServletContext().getInitParameter((String)key);
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

    // Create and configure bridge
    InjectorProvider injectorProvider = config.injectorProvider;
    if (injectorProvider == null) {
      throw new UnavailableException("No inject implementation selected");
    } else {
      servletLogger.info("Using inject implementation " + injectorProvider.getValue());
    }

    //
    this.config = config;
    this.handler = null;
    this.path = path;
    this.bundleName = servletConfig.getInitParameter(BUNDLE_NAME);
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

  private void refresh() throws Exception {
    if (bridge == null) {

      // Get asset server
      AssetServer server = (AssetServer)getServletContext().getAttribute("asset.server");
      if (server == null) {
        server = new AssetServer();
        getServletContext().setAttribute("asset.server", server);
      }

      //
      BridgeContext bridgeContext = new AbstractBridgeContext() {
        final ResourceResolver resolver = new ResourceResolver() {
          public URL resolve(String uri) {
            try {
              return getServletContext().getResource(uri);
            }
            catch (MalformedURLException e) {
              return null;
            }
          }
        };
        public ReadFileSystem<?> getResourcePath() {
          return WarFileSystem.create(getServletContext(), "/WEB-INF/");
        }
        public ReadFileSystem<?> getClassPath() {
          return WarFileSystem.create(getServletContext(), "/WEB-INF/classes/");
        }
        public ClassLoader getClassLoader() {
          return getServletContext().getClassLoader();
        }
        public String getInitParameter(String name) {
          return getServletContext().getInitParameter(name);
        }
        public ResourceResolver getResolver() {
          return resolver;
        }
        public Object getAttribute(String key) {
          return getServletContext().getAttribute(key);
        }
        public void setAttribute(String key, Object value) {
          getServletContext().setAttribute(key, value);
        }
      };

      //
      ResourceResolver resolver = new ResourceResolver() {
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
      Injector injector = config.injectorProvider.get();
      if (injector instanceof SpringInjector) {
        SpringInjector springInjector = (SpringInjector)injector;
        Object parent = getServletContext().getAttribute("org.springframework.web.context.WebApplicationContext.ROOT");
        if (parent != null) {
          springInjector.setParent(parent);
        }
      }

      //
      if (injector.isProvided()) {
        bridge = new ProvidedBridge(bridgeContext, this.config, server, resolver, injector);
      } else {
        bridge = new ApplicationBridge(bridgeContext, this.config, server, resolver, injector);
      }
    }

    //
    Completion<Boolean> refresh = bridge.refresh();
    if (refresh.isFailed()) {
      throw refresh.getCause();
    } else if (refresh.get()) {
      if (handler != null) {
        Tools.safeClose(handler);
        handler = null;
        applicationContext = null;
      }
    }

    //
    if (handler == null) {
      this.handler = new Handler(bridge);
      this.applicationContext = new ServletApplicationContext(getServletContext().getClassLoader(), bundleName);
    }
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    //
    ServletRequestContext ctx = new ServletRequestContext(config.requestEncoding, req, resp, path);

    //
    ServletWebBridge bridge = new ServletWebBridge(this, ctx);

    // Do we need to send a server resource ?
    if (ctx.getRequestPath().length() > 1 && !ctx.getRequestPath().startsWith("/WEB-INF/")) {
      URL url = getServletContext().getResource(ctx.getRequestPath());
      if (url != null) {
        RequestDispatcher dispatcher = getServletContext().getNamedDispatcher("default");
        dispatcher.include(bridge.getRequestContext().req, bridge.getResponse());
        return;
      }
    }

    //
    try {
      refresh();
    }
    catch (CompilationException e) {
      ctx.send(e);
      return;
    }
    catch (Exception e) {
      throw wrap(e);
    }

    //
    try {
      handler.handle(bridge);
    }
    catch (Throwable throwable) {
      throw wrap(throwable);
    }
  }

  @Override
  public void destroy() {
/*
    if (module != null) {
      if (module.release()) {
        // Should dispose module (todo later)
      }
    }
*/
    if (handler != null) {
      Tools.safeClose(handler);
      this.handler = null;
    }
  }
}
