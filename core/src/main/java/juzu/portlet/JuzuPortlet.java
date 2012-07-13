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

package juzu.portlet;

import juzu.PropertyType;
import juzu.impl.asset.AssetServer;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.compiler.CompilationError;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.war.WarFileSystem;
import juzu.impl.bridge.spi.portlet.PortletActionBridge;
import juzu.impl.bridge.spi.portlet.PortletRenderBridge;
import juzu.impl.bridge.spi.portlet.PortletResourceBridge;
import juzu.impl.common.Logger;
import juzu.impl.common.SimpleMap;
import juzu.impl.common.Tools;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
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
import java.util.Collection;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JuzuPortlet implements Portlet, ResourceServingPortlet {

  /** . */
  public static final class PORTLET_MODE extends PropertyType<PortletMode> {}

  /** . */
  public static final class WINDOW_STATE extends PropertyType<WindowState> {}

  /** . */
  public static final PORTLET_MODE PORTLET_MODE = new PORTLET_MODE();

  /** . */
  public static final WINDOW_STATE WINDOW_STATE = new WINDOW_STATE();

  /** . */
  private BridgeConfig bridgeConfig;

  /** . */
  private Bridge bridge;

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
    ReadFileSystem<?> sourcePath = srcPath != null ? new DiskFileSystem(new File(srcPath)) : WarFileSystem.create(config.getPortletContext(), "/WEB-INF/src/");

    //
    Bridge bridge = new Bridge();
    bridge.config = bridgeConfig;
    bridge.resources = WarFileSystem.create(config.getPortletContext(), "/WEB-INF/");
    bridge.server = server;
    bridge.log = log;
    bridge.sourcePath = sourcePath;
    bridge.classes = WarFileSystem.create(config.getPortletContext(), "/WEB-INF/classes/");

    //
    this.bridgeConfig = bridgeConfig;
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
  }

  /**
   * Returns the application name to use using the <code>juzu.app_name</code> init parameter of the portlet deployment
   * descriptor. Subclass can override it to provide a custom application name.
   *
   * @param config the portlet config
   * @return the application name
   */
  protected String getApplicationName(PortletConfig config) {
    return config.getInitParameter("juzu.app_name");
  }

  private PortletException wrap(Throwable e) {
    return e instanceof PortletException ? (PortletException)e : new PortletException("Could not find an application to start", e);
  }

  public void processAction(ActionRequest req, ActionResponse resp) throws PortletException, IOException {
    PortletActionBridge requestBridge = new PortletActionBridge(req, resp, bridge.config.isProd());
    try {
      bridge.processAction(requestBridge);
    }
    catch (Throwable e) {
      throw wrap(e);
    }
    finally {
      requestBridge.close();
    }
  }

  public void render(final RenderRequest req, final RenderResponse resp) throws PortletException, IOException {

    //
    PortletRenderBridge requestBridge = new PortletRenderBridge(bridge, req, resp, !bridgeConfig.isProd(), bridge.config.isProd());

    //
    try {
      bridge.render(requestBridge);
    }
    catch (Throwable e) {
      throw wrap(e);
    }
  }

  public void serveResource(final ResourceRequest req, final ResourceResponse resp) throws PortletException, IOException {
    boolean assetRequest = "assets".equals(req.getParameter("juzu.request"));

    //
    if (assetRequest && !bridgeConfig.isProd()) {
      String path = req.getResourceID();
      String contentType;
      InputStream in;
      if (bridge.runtime.getScriptManager().isClassPath(path)) {
        contentType = "text/javascript";
        in = bridge.runtime.getClassLoader().getResourceAsStream(path.substring(1));
      }
      else if (bridge.runtime.getStylesheetManager().isClassPath(path)) {
        contentType = "text/css";
        in = bridge.runtime.getClassLoader().getResourceAsStream(path.substring(1));
      }
      else {
        contentType = null;
        in = null;
      }
      if (in != null) {
        resp.setContentType(contentType);
        Tools.copy(in, resp.getPortletOutputStream());
      }
    }
    else {
      PortletResourceBridge requestBridge = new PortletResourceBridge(req, resp, !bridgeConfig.isProd(), bridge.config.isProd());
      try {
        bridge.serveResource(requestBridge);
      }
      catch (Throwable throwable) {
        throw wrap(throwable);
      }
    }
  }

  public void destroy() {
  }
}
