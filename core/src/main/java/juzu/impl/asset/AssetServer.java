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

package juzu.impl.asset;

import juzu.asset.AssetLocation;
import juzu.impl.plugin.application.Application;
import juzu.impl.common.Tools;
import juzu.impl.request.Request;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashSet;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetServer {

  /** . */
  HashSet<Application> runtimes = new HashSet<Application>();

  /** . */
  private static final ThreadLocal<AssetServer> current = new ThreadLocal<AssetServer>();

  public AssetServer() {
  }

  public void register(Application assetManager) {
    runtimes.add(assetManager);
  }

  public void unregister(Application assetManager) {
    runtimes.remove(assetManager);
  }

  public boolean doGet(String path, ServletContext ctx, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (path != null && path.length() > 0) {
      for (Application runtime : runtimes) {
        Iterable<AssetManager> resolvers = runtime.resolveBeans(AssetManager.class);
        for (AssetManager resolver : resolvers) {
          // For now we only have resource of URL type ...
          URL content = resolver.resolveApplicationAssetResource(path);
          if (content == null) {
            // It could be a server resource like an image
            content = ctx.getResource(path);
          }
          if (content != null) {
            InputStream in;
            long lastModified;
            URLConnection conn = content.openConnection();
            lastModified = conn.getLastModified();
            String etag = Tools.etag(path, lastModified);
            Enumeration<String> matches = req.getHeaders("If-None-Match");
            if (matches.hasMoreElements() && matches.nextElement().equals(etag)) {
              resp.setStatus(304);
            } else {
              in = conn.getInputStream();
              int pos = path.lastIndexOf('/');
              String name = pos == -1 ? path : path.substring(pos + 1);
              resp.setHeader("ETag", etag);
              Integer maxAge = resolver.maxAge;
              if (maxAge != null && maxAge > 0) {
                resp.setHeader("Cache-Control", "max-age=" + maxAge);
              }
              String contentType = ctx.getMimeType(name);
              if (contentType != null) {
                resp.setContentType(contentType);
              }
              Tools.copy(in, resp.getOutputStream());
            }
            return true;
          }
        }
      }
    }
    return false;
  }

  public static String renderAssetURL(String path) throws NullPointerException {
    return renderAssetURL(AssetLocation.APPLICATION, path);
  }

  public static String renderAssetURL(AssetLocation location, String uri) throws NullPointerException {
    Request current = Request.getCurrent();
    if (current != null) {
      StringBuilder buffer = new StringBuilder();
      switch (location) {
        case APPLICATION:
          current.renderAssetURL(location, uri, buffer);
          break;
        default:
          current.renderAssetURL(location, uri, buffer);
          break;
      }
      return buffer.toString();
    } else {
      return null;
    }
  }
}
