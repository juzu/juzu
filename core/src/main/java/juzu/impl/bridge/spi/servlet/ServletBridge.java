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

import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.bridge.spi.web.Handler;
import juzu.impl.common.Tools;
import juzu.impl.plugin.application.descriptor.ApplicationModuleDescriptor;
import juzu.impl.common.Logger;
import juzu.impl.common.SimpleMap;
import juzu.impl.resource.ResourceResolver;

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
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletBridge extends HttpServlet {

  /** . */
  ServletModule module;

  /** . */
  Logger log;

  /** . */
  private BridgeConfig config;

  /** . */
  private Handler handler;

  /** . */
  private String path;

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
    this.path = null;
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
      Handler handler = null;
      try {
        handler = new Handler(bridge);
      }
      catch (Exception e) {
        throw wrap(e);
      }

      //
      this.handler = handler;
      this.path = path;
    }
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (req.getMethod().equals("GET") || req.getMethod().equals("POST")) {

      //
      refresh();

      //
      ServletWebBridge bridge = new ServletWebBridge(req, resp, path);

      // Do we need to send a server resource ?
      if (bridge.getRequestPath().length() > 1 && !bridge.getRequestPath().startsWith("/WEB-INF/")) {
        URL url = getServletContext().getResource(bridge.getRequestPath());
        if (url != null) {
          RequestDispatcher dispatcher = getServletContext().getNamedDispatcher("default");
          dispatcher.include(bridge.getRequest(), bridge.getResponse());
          return;
        }
      }

      //
      try {
        handler.handle(bridge);
      }
      catch (Throwable throwable) {
        throw wrap(throwable);
      }
    } else {
      super.service(req, resp);
    }
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
