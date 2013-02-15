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
import juzu.io.Stream;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class WebRenderBridge extends WebMimeBridge implements RenderBridge {

  WebRenderBridge(
      Application application,
      Handler handler,
      WebBridge http,
      Method<?> target,
      Map<String, String[]> parameters) {
    super(application, handler, http, target, parameters);
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
  boolean send() throws IOException {
    if (super.send()) {
      return true;
    } else if (response instanceof Response.Content<?>) {

      //
      Response.Content<?> content = (Response.Content)response;

      //
      PropertyMap properties = response.getProperties();

      //
      http.setContentType(content.getMimeType());

      //
      Integer status = content.getStatus();
      if (status != null) {
        http.setStatus(status);
      }

      //
      Iterable<Map.Entry<String, String[]>> headers = properties.getValues(PropertyType.HEADER);
      if (headers != null) {
        for (Map.Entry<String, String[]> entry : headers) {
          http.setHeader(entry.getKey(), entry.getValue()[0]);
        }
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
        String title = properties.getValue(PropertyType.TITLE);
        if (title != null) {
          writer.append("<title>");
          writer.append(title);
          writer.append("</title>\n");
        }

        //
        Iterable<Map.Entry<String, String>> metaProps = properties.getValues(PropertyType.META_TAG);
        if (metaProps != null) {
          for (Map.Entry<String, String> meta : metaProps) {
            writer.append("<meta name=\"");
            writer.append(meta.getKey());
            writer.append("\" content=\"");
            writer.append(meta.getValue());
            writer.append("\">\n");
          }
        }

        //
        Iterable<Asset> stylesheetProps = properties.getValues(PropertyType.STYLESHEET);
        if (stylesheetProps != null) {
          Iterable<Asset.Value> stylesheets =  handler.getBridge().application.getStylesheetManager().resolveAssets(stylesheetProps);
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
        }

        //
        Iterable<Asset> scriptsProp = properties.getValues(PropertyType.SCRIPT);
        if (scriptsProp != null) {
          Iterable<Asset.Value> scripts = handler.getBridge().application.getScriptManager().resolveAssets(scriptsProp);
          for (Asset.Value script : scripts) {
            writer.append("<script type=\"text/javascript\" src=\"");
            writer.append(getAssetURL(script));
            writer.append("\"></script>\n");
          }
        }

        //
        writer.append("</head>\n");
        writer.append("<body>\n");

        // Send response
        if (content.getKind() == Stream.Char.class) {
          ((Response.Content<Stream.Char>)content).send(new AppendableStream(writer));
        } else {
          throw new UnsupportedOperationException("Not yet handled");
        }

        //
        writer.append("</body>\n");
        writer.append("</html>\n");
      }
      finally {
        Tools.safeClose(writer);
      }

      //
      return true;
    } else {
      return false;
    }
  }

  private String getAssetURL(Asset.Value asset) throws IOException {
    StringBuilder url = new StringBuilder();
    String uri = asset.getURI();
    http.renderAssetURL(asset.getLocation(), uri, url);
    return url.toString();
  }
}
