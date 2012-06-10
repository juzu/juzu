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

import juzu.asset.AssetType;
import juzu.impl.application.ApplicationContext;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetServer;
import juzu.impl.asset.ManagerQualifier;
import juzu.impl.request.spi.servlet.ServletBridgeContext;
import juzu.impl.request.spi.servlet.ServletRequestBridge;
import juzu.impl.utils.Logger;
import juzu.impl.utils.Tools;
import juzu.test.AbstractHttpTestCase;
import juzu.test.protocol.mock.MockApplication;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class InvocationServlet extends HttpServlet {

  /**
   * Returns an asset server associated with the specified context or null if it does not exist.
   *
   * @param context the related context
   * @return the related server
   * @throws NullPointerException if the context argument is null
   */
  public static AssetServer getServer(ServletContext context) throws NullPointerException {
    if (context == null) {
      throw new NullPointerException("No null context accepted");
    }
    return registry.get(context.getContextPath());
  }

  /** . */
  private static final ConcurrentHashMap<String, AssetServer> registry = new ConcurrentHashMap<String, AssetServer>();

  /** . */
  private final Logger log = new Logger() {
    public void log(CharSequence msg) {
      System.out.print(msg);
    }

    public void log(CharSequence msg, Throwable t) {
      System.err.println(msg);
      t.printStackTrace(System.err);
    }
  };

  /** . */
  private MockApplication<?> application;

  /** . */
  private ServletBridgeContext bridge;

  @Override
  public void init() throws ServletException {
    try {
      MockApplication<?> application = AbstractHttpTestCase.getApplication();

      // Bind the asset manager
      AssetManager scriptManager = new AssetManager(AssetType.SCRIPT);
      application.bindBean(
        AssetManager.class,
        Collections.<Annotation>singleton(new ManagerQualifier(AssetType.SCRIPT)),
        scriptManager);
      AssetManager stylesheetManager = new AssetManager(AssetType.STYLESHEET);
      application.bindBean(
        AssetManager.class,
        Collections.<Annotation>singleton(new ManagerQualifier(AssetType.STYLESHEET)),
        stylesheetManager);

      //
      application.init();

      //
      this.application = application;
      this.bridge = new ServletBridgeContext(application.getContext(), scriptManager, stylesheetManager, log);
    }
    catch (Exception e) {
      throw new ServletException(e);
    }
  }

  @Override
  public void destroy() {
    getServletContext().removeAttribute("asset.server");
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
      InputStream in;
      if (bridge.getScriptManager().isClassPath(path)) {
        in = application.getContext().getClassLoader().getResourceAsStream(path.substring(1));
      }
      else {
        in = getServletContext().getResourceAsStream(path);
      }
      if (in != null) {
        resp.setContentType(contentType);
        OutputStream out = resp.getOutputStream();
        Tools.copy(in, out);
      }
      else {
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
