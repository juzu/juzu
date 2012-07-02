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

import juzu.Response;
import juzu.asset.Asset;
import juzu.impl.inject.ScopedContext;
import juzu.impl.bridge.spi.RenderBridge;
import juzu.impl.utils.Tools;
import juzu.io.AppendableStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RenderBridgeImpl extends MimeBridgeImpl implements RenderBridge {

  /** . */
  private Response.Content response;

  /** . */
  private Iterable<Asset.Value> scripts;

  /** . */
  private Iterable<Asset.Value> stylesheets;

  /** Unused for now. */
  private String title;

  RenderBridgeImpl(
      HttpServletBridgeContext context,
      HttpServletRequest req,
      HttpServletResponse resp,
      String methodId,
      Map<String, String[]> parameters) {
    super(context, req, resp, methodId, parameters);
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setResponse(Response response) throws IllegalStateException, IOException {
    if (response instanceof Response.Content) {
      if (response instanceof Response.Render) {
        Response.Render render = (Response.Render)response;
        try {
          Iterable<Asset.Value> scripts = context.scriptManager.resolveAssets(render.getScripts());
          Iterable<Asset.Value> stylesheets = context.scriptManager.resolveAssets(render.getStylesheets());
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

  private void renderAssetURL(Asset.Value asset, Appendable appendable) throws IOException {
    String uri = asset.getURI();
    switch (asset.getLocation()) {
      case SERVER:
        if (!uri.startsWith("/")) {
          appendable.append(req.getContextPath());
          appendable.append('/');
        }
        appendable.append(uri);
        break;
      case CLASSPATH:
        appendable.append(req.getContextPath());
        if (!uri.startsWith("/")) {
          appendable.append('/');
        }
        appendable.append(uri);
        break;
      case EXTERNAL:
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
          for (Asset.Value stylesheet : stylesheets) {
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
          for (Asset.Value script : scripts) {
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
        response.send(new AppendableStream(writer));

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
