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

package juzu.impl.asset;

import juzu.impl.plugin.application.ApplicationLifeCycle;
import juzu.impl.common.Tools;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetServer {

  /** . */
  HashSet<ApplicationLifeCycle<?, ?>> runtimes = new HashSet<ApplicationLifeCycle<?, ?>>();

  public AssetServer() {
  }

  public void register(ApplicationLifeCycle<?, ?> assetManager) {
    runtimes.add(assetManager);
  }

  public void unregister(ApplicationLifeCycle<?, ?> assetManager) {
    runtimes.remove(assetManager);
  }

  public boolean doGet(String path, ServletContext ctx, HttpServletResponse resp) throws ServletException, IOException {
    if (path != null && path.length() > 0) {
      for (ApplicationLifeCycle<?, ?> runtime : runtimes) {
        String contentType;
        InputStream in;
        URL url = runtime.getScriptManager().resolveAsset(path);
        if (url != null) {
          contentType = "text/javascript";
          in = url.openStream();
        } else {
          contentType = null;
          in = null;
        }
        if (in == null) {
          url = runtime.getStylesheetManager().resolveAsset(path);
          if (url != null) {
            contentType = "text/css";
            in = runtime.getContext().getClassLoader().getResourceAsStream(path.substring(1));
          }
        }

        // It could be a server resource like an image
        if (in == null) {
          in = ctx.getResourceAsStream(path);
          if (in != null) {
            int pos = path.lastIndexOf('/');
            String name = pos == -1 ? path : path.substring(pos + 1);
            contentType = ctx.getMimeType(name);
          } else {
            contentType = null;
            in = null;
          }
        }
        if (in != null) {
          if (contentType != null) {
            resp.setContentType(contentType);
          }
          Tools.copy(in, resp.getOutputStream());
          return true;
        }
      }
    }
    return false;
  }
}
