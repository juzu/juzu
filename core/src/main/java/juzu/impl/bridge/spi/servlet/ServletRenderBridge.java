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

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.asset.Asset;
import juzu.impl.plugin.application.Application;
import juzu.impl.inject.ScopedContext;
import juzu.impl.bridge.spi.RenderBridge;
import juzu.impl.common.Tools;
import juzu.impl.request.Method;
import juzu.io.AppendableStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletRenderBridge extends ServletMimeBridge implements RenderBridge {

  /** . */
  private Response.Content response;

  /** . */
  private Iterable<Asset.Value> scripts;

  /** . */
  private Iterable<Asset.Value> stylesheets;

  /** . */
  private Map<String, String> responseMetaTags;

  /** Unused for now. */
  private String title;

  ServletRenderBridge(
      Application application,
      Handler handler,
      HttpServletRequest req,
      HttpServletResponse resp,
      Method<?> target,
      Map<String, String[]> parameters) {
    super(application, handler, req, resp, target, parameters);
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setResponse(Response response) throws IllegalStateException, IOException {
    super.setResponse(response);
    if (response instanceof Response.Content) {
      Response.Content content = (Response.Content)response;
      try {

        //
        PropertyMap properties = response.getProperties();
        Iterable<Map.Entry<String, String>> metaProps = properties.getValues(PropertyType.META_TAG);
        Iterable<Asset> scriptsProp = properties.getValues(PropertyType.SCRIPT);
        Iterable<Asset> stylesheetProps = properties.getValues(PropertyType.STYLESHEET);

        //
        Map<String, String > metaTags = Collections.emptyMap();
        if (metaProps != null) {
          for (Map.Entry<String, String> entry : metaProps) {
            if (metaTags.isEmpty()) {
              metaTags = new HashMap<String, String>();
            }
            metaTags.put(entry.getKey(), entry.getValue());
          }
        }

        //
        Iterable<Asset.Value> stylesheets = Collections.emptyList();
        if (stylesheetProps != null) {
          stylesheets = handler.bridge.application.getStylesheetManager().resolveAssets(stylesheetProps);
        }

        //
        Iterable<Asset.Value> scripts = Collections.emptyList();
        if (scriptsProp != null) {
          scripts = handler.bridge.application.getScriptManager().resolveAssets(scriptsProp);
        }

        //
        this.scripts = scripts;
        this.stylesheets = stylesheets;
        this.responseMetaTags = metaTags;
      }
      catch (IllegalArgumentException e) {
        response = Response.content(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      }

      //
      this.response = (Response.Content)response;
    } else {
      throw new IllegalArgumentException();
    }
  }

  private String getAssetURL(Asset.Value asset) {
    StringBuilder sb;
    String url;
    String uri = asset.getURI();
    switch (asset.getLocation()) {
      case SERVER:
        sb = new StringBuilder();
        if (!uri.startsWith("/")) {
          sb.append(req.getContextPath());
          sb.append('/');
        }
        sb.append(uri);
        url = sb.toString();
        break;
      case CLASSPATH:
        if (handler.bridge.getConfig().isProd()) {
          sb = new StringBuilder();
          sb.append(req.getContextPath()).append("/assets");
          if (!uri.startsWith("/")) {
            sb.append('/');
          }
          sb.append(uri);
          url = sb.toString();
        }
        else {
          throw new UnsupportedOperationException("not yet done");
        }
        break;
      case URL:
        url = uri;
        break;
      default:
        throw new AssertionError();
    }
    return url;
  }

  @Override
  public void end() {
    super.end();

    //
    ScopedContext context = getFlashContext(false);
    if (context != null) {
      context.close();
    }
  }

  @Override
  void send() throws IOException {
    if (response != null) {

      //
      resp.setContentType(response.getMimeType());

      //
      Integer status = response.getStatus();
      if (status != null) {
        resp.setStatus(status);
      }

      //
      for (Map.Entry<String, String[]> entry : responseHeaders.entrySet()) {
        resp.setHeader(entry.getKey(), entry.getValue()[0]);
      }

      //
      PrintWriter writer = null;
      try {
        writer = resp.getWriter();

        //
        writer.println("<!DOCTYPE html>");
        writer.println("<html>");
        writer.println("<head>");

        //
        if (title != null) {
          writer.print("<title>");
          writer.print(title);
          writer.println("</title>");
        }

        //
        for (Map.Entry<String, String> meta : responseMetaTags.entrySet()) {
          writer.print("<meta name=\"");
          writer.append(meta.getKey());
          writer.append("\" content=\"");
          writer.append(meta.getValue());
          writer.println("\">");
        }

        //
        for (Asset.Value stylesheet : stylesheets) {
          String path = stylesheet.getURI();
          int pos = path.lastIndexOf('.');
          String ext = pos == -1 ? "css" : path.substring(pos + 1);
          writer.print("<link rel=\"stylesheet\" type=\"text/");
          writer.print(ext);
          writer.print("\" href=\"");
          writer.append(getAssetURL(stylesheet));
          writer.println("\"></link>");
        }

        //
        for (Asset.Value script : scripts) {
          writer.print("<script type=\"text/javascript\" src=\"");
          writer.append(getAssetURL(script));
          writer.println("\"></script>");
        }

        //
        writer.println("</head>");
        writer.println("<body>");

        // Send response
        response.send(new AppendableStream(writer));

        //
        writer.println("</body>");
        writer.println("</html>");
      }
      finally {
        Tools.safeClose(writer);
      }
    }
  }
}
