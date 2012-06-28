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

package juzu.test.protocol.http;

import juzu.impl.application.ApplicationContext;
import juzu.impl.application.ApplicationRuntime;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetServer;
import juzu.impl.request.spi.servlet.ServletBridgeContext;
import juzu.impl.request.spi.servlet.ServletRequestBridge;
import juzu.test.AbstractHttpTestCase;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class InvocationServlet extends HttpServlet {


  /** . */
  private ApplicationRuntime<?, ?, ?> application;

  /** . */
  private ServletBridgeContext bridge;

  /** . */
  private AssetServer assetServer = new AssetServer();

  @Override
  public void init() throws ServletException {
    try {
      ApplicationRuntime<?, ?, ?> application = AbstractHttpTestCase.getApplication();

      //
      application.boot();

      // Bind the asset managers
      AssetManager scriptManager = application.getScriptManager();
      AssetManager stylesheetManager = application.getStylesheetManager();

      //
      assetServer.register(application);

      //
      this.application = application;
      this.bridge = new ServletBridgeContext(
          application.getContext(),
          scriptManager,
          stylesheetManager,
          application.getLogger());
    }
    catch (Exception e) {
      throw new ServletException(e);
    }
  }

  @Override
  public void destroy() {
    assetServer.unregister(application);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String path = req.getRequestURI().substring(req.getContextPath().length());
    String contentType;
    if (path.endsWith(".js")) {
      contentType = "text/javascript";
    }
    else if (path.endsWith(".css")) {
      contentType = "text/css";
    }
    else {
      contentType = null;
    }
    if (contentType != null) {
      if (!assetServer.doGet(path, getServletContext(), resp)) {
        resp.sendError(404, "Path " + path + " could not be resolved");
      }
    }
    else {
      ServletRequestBridge requestBridge = bridge.create(req, resp);
      ApplicationContext context = application.getContext();
      context.invoke(requestBridge);
    }
  }
}
