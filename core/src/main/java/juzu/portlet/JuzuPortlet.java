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
import juzu.impl.application.ApplicationException;
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
import juzu.impl.utils.Logger;
import juzu.impl.utils.SimpleMap;
import juzu.impl.utils.Tools;
import juzu.impl.utils.TrimmingException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceServingPortlet;
import javax.portlet.WindowState;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

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
  private String srcPath;

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
    BridgeConfig bridgeConfig = new BridgeConfig(new SimpleMap<String, String>() {
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

    //
    ReadFileSystem<?> sourcePath = srcPath != null ? new DiskFileSystem(new File(srcPath)) : WarFileSystem.create(config.getPortletContext(), "/WEB-INF/src/");

    //
    Bridge bridge = new Bridge();
    bridge.config = bridgeConfig;
    bridge.resources = WarFileSystem.create(config.getPortletContext(), "/WEB-INF/");
    bridge.server = server;
    bridge.log = log;
    bridge.sourcePath = sourcePath;

    //
    this.bridgeConfig = bridgeConfig;
    this.srcPath = config.getInitParameter("juzu.src_path");
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

  private PortletException wrap(Exception e) {
    return e instanceof PortletException ? (PortletException)e : new PortletException("Could not find an application to start", e);
  }

  public void processAction(ActionRequest req, ActionResponse resp) throws PortletException, IOException {
    PortletActionBridge requestBridge = new PortletActionBridge(req, resp, bridge.config.prod);
    try {
      bridge.runtime.getContext().invoke(requestBridge);
    }
    catch (ApplicationException e) {
      // For now we do that until we find something better specially for the dev mode
      throw new PortletException(e.getCause());
    }
    finally {
      requestBridge.close();
    }
  }

  /**
   * Purge the session.
   *
   * @param req the request owning the session
   */
  private void purgeSession(PortletRequest req) {
    PortletSession session = req.getPortletSession();
    for (String key : new HashSet<String>(session.getAttributeMap().keySet())) {
      session.removeAttribute(key);
    }
  }

  public void render(final RenderRequest req, final RenderResponse resp) throws PortletException, IOException {

    //
    Collection<CompilationError> errors;
    try {
      errors = bridge.boot();
    }
    catch (Exception e) {
      throw wrap(e);
    }

    //
    if (errors == null || errors.isEmpty()) {
      if (errors != null) {
        purgeSession(req);
      }

      //
      final PortletRenderBridge requestBridge = new PortletRenderBridge(bridge, req, resp, !bridgeConfig.prod, bridge.config.prod);

      //
      try {
        TrimmingException.invoke(new TrimmingException.Callback() {
          public void call() throws Throwable {
            try {
              bridge.runtime.getContext().invoke(requestBridge);
            }
            catch (ApplicationException e) {
              throw e.getCause();
            }
          }
        });
      }
      catch (TrimmingException e) {
        if (bridgeConfig.prod) {
          throw new PortletException(e.getSource());
        }
        else {
          renderThrowable(resp.getWriter(), e);
        }
      } finally {
        requestBridge.close();
      }
    }
    else {
      renderErrors(resp.getWriter(), errors);
    }
  }

  public void serveResource(final ResourceRequest req, final ResourceResponse resp) throws PortletException, IOException {
    boolean assetRequest = "assets".equals(req.getParameter("juzu.request"));

    //
    if (assetRequest && !bridgeConfig.prod) {
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
      final PortletResourceBridge requestBridge = new PortletResourceBridge(req, resp, !bridgeConfig.prod, bridge.config.prod);

      //
      try {
        TrimmingException.invoke(new TrimmingException.Callback() {
          public void call() throws Throwable {
            try {
              bridge.runtime.getContext().invoke(requestBridge);
            }
            catch (ApplicationException e) {
              throw e.getCause();
            }
          }
        });
      }
      catch (TrimmingException e) {
        // Internal server error
        resp.setProperty(ResourceResponse.HTTP_STATUS_CODE, "500");

        //
        logThrowable(e);

        //
        if (!bridgeConfig.prod) {
          PrintWriter writer = resp.getWriter();
          writer.print("<html>\n");
          writer.print("<head>\n");
          writer.print("</head>\n");
          writer.print("<body>\n");
          renderThrowable(writer, e);
          writer.print("</body>\n");
        }
      } finally {
        requestBridge.close();
      }
    }
  }

  private void sendJuzuCSS(PrintWriter writer) throws IOException {
    // Get CSS
    URL cssURL = JuzuPortlet.class.getResource("juzu.css");
    String css = Tools.read(cssURL);
    css = css.replace("\"", "\\\"");
    css = css.replace("'", "\\'");
    css = css.replace("\n", "\\n");

    //
    writer.append("<script type='text/javascript'>\n");
    writer.append("var styleElement = document.createElement('style');\n");
    writer.append("var css = '");
    writer.append(css);
    writer.append("';\n");
    writer.append("styleElement.type = 'text/css';\n");
    writer.append("if (styleElement.styleSheet) {;\n");
    writer.append("styleElement.styleSheet.cssText = css;\n");
    writer.append("} else {\n");
    writer.append("styleElement.appendChild(document.createTextNode(css));\n");
    writer.append("}\n");
    writer.append("document.getElementsByTagName(\"head\")[0].appendChild(styleElement);\n");
    writer.append("</script>\n");
  }

  private void logThrowable(Throwable t) {
    bridge.log.log(t.getMessage(), t);
  }

  private void logErrors(Collection<CompilationError> errors) {
    // Todo format that better like it is in renderErrors
    StringBuilder sb = new StringBuilder("Compilation errors:\n");
    for (CompilationError error : errors) {
      if (error.getSourceFile() != null) {
        sb.append(error.getSourceFile().getAbsolutePath());
      }
      else {
        sb.append(error.getSource());
      }
      sb.append(':').append(error.getLocation().getLine()).append(':').append(error.getMessage()).append('\n');
    }
    bridge.log.log(sb.toString());
  }

  private void renderThrowable(PrintWriter writer, Throwable t) throws PortletException, IOException {
    // Trim the stack trace to remove stuff we don't want to see
    int size = 0;
    StackTraceElement[] trace = t.getStackTrace();
    for (StackTraceElement element : trace) {
      if (element.getClassName().equals(JuzuPortlet.class.getName())) {
        break;
      }
      else {
        size++;
      }
    }
    StackTraceElement[] ourTrace = new StackTraceElement[size];
    System.arraycopy(trace, 0, ourTrace, 0, ourTrace.length);
    t.setStackTrace(ourTrace);

    //
    sendJuzuCSS(writer);

    // We hack a bit
    final AtomicBoolean open = new AtomicBoolean(false);
    PrintWriter formatter = new PrintWriter(writer) {
      @Override
      public void println(Object x) {
        if (open.get()) {
          super.append("</ul></pre>");
        }
        super.append("<div class=\"juzu-message\">");
        super.append(String.valueOf(x));
        super.append("</div>");
        open.set(false);
      }

      @Override
      public void println(String x) {
        if (!open.get()) {
          super.append("<pre><ul>");
          open.set(true);
        }
        super.append("<li><span>");
        super.append(x);
        super.append("</span></li>");
      }

      @Override
      public void println() {
        // Do nothing
      }
    };

    //
    writer.append("<div class=\"juzu\">");
    writer.append("<div class=\"juzu-box\">");

    // We hack a bit with our formatter
    t.printStackTrace(formatter);

    //
    if (open.get()) {
      writer.append("</ul></pre>");
    }

    //
    writer.append("</div>");
    writer.append("</div>");
  }

  private void renderErrors(PrintWriter writer, Collection<CompilationError> errors) throws PortletException, IOException {
    sendJuzuCSS(writer);

    //
    writer.append("<div class=\"juzu\">");
    for (CompilationError error : errors) {
      writer.append("<div class=\"juzu-box\">");
      writer.append("<div class=\"juzu-message\">").append(error.getMessage()).append("</div>");

      // Display the source code
      File source = error.getSourceFile();
      if (source != null) {
        int line = error.getLocation().getLine();
        int from = line - 2;
        int to = line + 3;
        BufferedReader reader = new BufferedReader(new FileReader(source));
        int count = 1;
        writer.append("<pre><ol start=\"").append(String.valueOf(from)).append("\">");
        for (String s = reader.readLine();s != null;s = reader.readLine()) {
          if (count >= from && count < to) {
            if (count == line) {
              writer.append("<li><span class=\"error\">").append(s).append("</span></li>");
            }
            else {
              writer.append("<li><span>").append(s).append("</span></li>");
            }
          }
          count++;
        }
        writer.append("</ol></pre>");
      }
      writer.append("</div>");
    }
    writer.append("</div>");
  }

  public void destroy() {
  }
}
