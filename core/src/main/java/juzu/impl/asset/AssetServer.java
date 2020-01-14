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
import juzu.impl.common.Timestamped;
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
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetServer {

  /** Application -> Dynamic. */
  HashMap<Application, Boolean> runtimes = new HashMap<Application, Boolean>();

  /** . */
  private static final ThreadLocal<AssetServer> current = new ThreadLocal<AssetServer>();

  public AssetServer() {
  }

  public void register(Application assetManager, boolean cacheAssets) {
    runtimes.put(assetManager, cacheAssets);
  }

  public void unregister(Application assetManager) {
    runtimes.remove(assetManager);
  }

  public boolean doGet(String path, ServletContext ctx, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (path != null && path.length() > 0) {
      for (Map.Entry<Application, Boolean> runtime : runtimes.entrySet()) {
        Iterable<AssetManager> resolvers = runtime.getKey().resolveBeans(AssetManager.class);
        for (AssetManager resolver : resolvers) {
          // For now we only have resource of URL type ...
          AssetResource content = resolver.resolveApplicationAssetResource(path);
          if (content == null) {
            // It could be a server resource like an image
            URL resource = ctx.getResource(path);
            if (resource != null) {
              content = new AssetResource(resource, null);
            }
          }
          if (content != null && content.url != null) {
            InputStream in;
            long lastModified;
            URLConnection conn = content.url.openConnection();
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
              boolean cacheAssets = runtime.getValue();
              if (cacheAssets) {
                int maxAge = content.maxAge != null ? content.maxAge : 3600;
                if (maxAge > 0) {
                  resp.setHeader("Cache-Control", "max-age=" + maxAge);
                }
              } else {
                resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
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

  public static String renderAssetURLById(String id) throws NullPointerException {
    Request request = Request.getCurrent();
    if (request != null) {
      AssetManager assetManager = request.getApplication().resolveBean(AssetManager.class);
      if (assetManager != null) {
        Asset asset = assetManager.getAsset(id);
        if (asset != null) {
          String uri = asset.resolveURI(request.getRunMode().getMinifyAssets());
          return renderAssetURL(request, asset.getLocation(), uri);
        }
      }
    }
    return null;
  }

  public static String renderAssetURLByPath(String path) throws NullPointerException {
    Request request = Request.getCurrent();
    if (request != null) {
      return renderAssetURL(request, AssetLocation.APPLICATION, path);
    } else {
      return null;
    }
  }

  public static String renderAssetURLByPath(AssetLocation location, String path) throws NullPointerException {
    Request request = Request.getCurrent();
    if (request != null) {
      return renderAssetURL(request, location, path);
    } else {
      return null;
    }
  }

  private static String renderAssetURL(Request request, AssetLocation location, String uri) throws NullPointerException {
    StringBuilder buffer = new StringBuilder();
    switch (location) {
      case APPLICATION:
        request.renderAssetURL(location, uri, buffer);
        break;
      default:
        request.renderAssetURL(location, uri, buffer);
        break;
    }
    return buffer.toString();
  }
}
