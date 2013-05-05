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

package juzu.test.protocol.http;

import juzu.Response;
import juzu.impl.asset.Asset;
import juzu.impl.common.Logger;
import juzu.impl.common.MethodHandle;
import juzu.impl.inject.ScopedContext;
import juzu.impl.bridge.spi.RenderBridge;
import juzu.impl.common.Tools;
import juzu.impl.plugin.application.ApplicationLifeCycle;
import juzu.io.Streams;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RenderBridgeImpl extends MimeBridgeImpl implements RenderBridge {

  /** . */
  private final HttpServletImpl servlet;

  /** . */
  private Response.Content response;

  /** . */
  private Iterable<Asset> scripts;

  /** . */
  private Iterable<Asset> stylesheets;

  /** Unused for now. */
  private String title;

  RenderBridgeImpl(
      Logger log,
      HttpServletImpl servlet,
      ApplicationLifeCycle<?, ?> application,
      HttpServletRequest req,
      HttpServletResponse resp,
      MethodHandle target,
      Map<String, String[]> parameters) {
    super(log, application, req, resp, target, parameters);

    //
    this.servlet = servlet;
  }

  public void setResponse(Response response) throws IllegalStateException, IOException {
    if (response instanceof Response.Content) {
      if (response instanceof Response.Render) {
        Response.Render render = (Response.Render)response;
        try {
          Iterable<Asset> scripts = servlet.scriptManager.resolveAssets(render.getScripts());
          Iterable<Asset> stylesheets = servlet.stylesheetManager.resolveAssets(render.getStylesheets());
          this.scripts = scripts;
          this.stylesheets = stylesheets;
        }
        catch (IllegalArgumentException e) {
          response = Response.content(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
      }
      this.response = (Response.Content)response;
    } else {
      throw new IllegalArgumentException();
    }
  }

  private void renderAssetURL(Asset asset, Appendable appendable) throws IOException {
    String uri = asset.getURI();
    switch (asset.getLocation()) {
      case SERVER:
        if (!uri.startsWith("/")) {
          appendable.append(req.getContextPath());
          appendable.append('/');
        }
        appendable.append(uri);
        break;
      case APPLICATION:
        appendable.append(req.getContextPath());
        if (!uri.startsWith("/")) {
          appendable.append('/');
        }
        appendable.append(uri);
        break;
      case URL:
        appendable.append(uri);
        break;
    }
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

  public void close() {
    if (response != null) {

      Integer status = response.getStatus();
      if (status != null) {
        resp.setStatus(status);
      }

      //
      PrintWriter writer = null;
      try {
        writer = resp.getWriter();

        //
        writer.println("<!DOCTYPE html>");
        writer.println("<html>");

        //
        if (response instanceof Response.Render) {
          writer.println("<head>");
          for (Asset stylesheet : stylesheets) {
            String path = stylesheet.getURI();
            int pos = path.lastIndexOf('.');
            String ext = pos == -1 ? "css" : path.substring(pos + 1);
            writer.print("<link rel=\"stylesheet\" type=\"text/");
            writer.print(ext);
            writer.print("\" href=\"");
            renderAssetURL(stylesheet, writer);
            writer.println("\"></link>");
          }

          //
          for (Asset script : scripts) {
            writer.print("<script type=\"text/javascript\" src=\"");
            renderAssetURL(script, writer);
            writer.println("\"></script>");
          }

          //
          writer.println("</head>");
        }

        //
        resp.setContentType(response.getMimeType());

        //
        writer.println("<body>");

        // Send response
        response.send(Streams.flushable(writer));

        //
        writer.println("</body>");
        writer.println("</html>");
      }
      catch (IOException e) {
        // ????
        e.printStackTrace();
      }
      finally {
        Tools.safeClose(writer);
      }
    }
  }
}
