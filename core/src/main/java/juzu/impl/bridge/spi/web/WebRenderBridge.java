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

package juzu.impl.bridge.spi.web;

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

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class WebRenderBridge extends WebMimeBridge implements RenderBridge {

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

  WebRenderBridge(
      Application application,
      Handler handler,
      WebBridge http,
      Method<?> target,
      Map<String, String[]> parameters) {
    super(application, handler, http, target, parameters);
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setResponse(Response response) throws IllegalStateException, IOException {
    super.setResponse(response);
    if (response instanceof Response.Content) {
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
          stylesheets = handler.getBridge().application.getStylesheetManager().resolveAssets(stylesheetProps);
        }

        //
        Iterable<Asset.Value> scripts = Collections.emptyList();
        if (scriptsProp != null) {
          scripts = handler.getBridge().application.getScriptManager().resolveAssets(scriptsProp);
        }

        //
        this.scripts = scripts;
        this.stylesheets = stylesheets;
        this.responseMetaTags = metaTags;
      }
      catch (IllegalArgumentException e) {
        response = Response.content(500, e.getMessage());
      }

      //
      this.response = (Response.Content)response;
    } else {
      throw new IllegalArgumentException();
    }
  }

  private String getAssetURL(Asset.Value asset) throws IOException {
    StringBuilder url = new StringBuilder();
    String uri = asset.getURI();
    http.renderAssetURL(asset.getLocation(), uri, url);
    return url.toString();
  }

  @Override
  public void end() {
    super.end();

    //
    ScopedContext context = http.getFlashScope(false);
    if (context != null) {
      context.close();
    }
  }

  @Override
  void send() throws IOException {
    if (response != null) {

      //
      http.setContentType(response.getMimeType());

      //
      Integer status = response.getStatus();
      if (status != null) {
        http.setStatus(status);
      }

      //
      for (Map.Entry<String, String[]> entry : responseHeaders.entrySet()) {
        http.setHeader(entry.getKey(), entry.getValue()[0]);
      }

      //
      Writer writer = null;
      try {
        writer = http.getWriter();

        //
        writer.append("<!DOCTYPE html>\n");
        writer.append("<html>\n");
        writer.append("<head>\n");

        //
        if (title != null) {
          writer.append("<title>");
          writer.append(title);
          writer.append("</title>\n");
        }

        //
        for (Map.Entry<String, String> meta : responseMetaTags.entrySet()) {
          writer.append("<meta name=\"");
          writer.append(meta.getKey());
          writer.append("\" content=\"");
          writer.append(meta.getValue());
          writer.append("\">\n");
        }

        //
        for (Asset.Value stylesheet : stylesheets) {
          String path = stylesheet.getURI();
          int pos = path.lastIndexOf('.');
          String ext = pos == -1 ? "css" : path.substring(pos + 1);
          writer.append("<link rel=\"stylesheet\" type=\"text/");
          writer.append(ext);
          writer.append("\" href=\"");
          writer.append(getAssetURL(stylesheet));
          writer.append("\"></link>\n");
        }

        //
        for (Asset.Value script : scripts) {
          writer.append("<script type=\"text/javascript\" src=\"");
          writer.append(getAssetURL(script));
          writer.append("\"></script>\n");
        }

        //
        writer.append("</head>\n");
        writer.append("<body>\n");

        // Send response
        response.send(new AppendableStream(writer));

        //
        writer.append("</body>\n");
        writer.append("</html>\n");
      }
      finally {
        Tools.safeClose(writer);
      }
    }
  }
}
