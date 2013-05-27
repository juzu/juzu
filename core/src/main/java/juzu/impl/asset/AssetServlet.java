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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetServlet extends HttpServlet {

  @Override
  public void init() throws ServletException {
    AssetServer server = (AssetServer)getServletContext().getAttribute("asset.server");
    if (server == null) {
      server = new AssetServer();
      getServletContext().setAttribute("asset.server", server);
    }
  }

  @Override
  public void destroy() {
    getServletContext().removeAttribute("asset.server");
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String path = req.getPathInfo();
    if (path != null) {
      AssetServer server = (AssetServer)getServletContext().getAttribute("asset.server");
      if (server != null) {
        if (!server.doGet(path, getServletContext(), resp)) {
          resp.sendError(404, "No resource found " + path);
        }
      }
      else {
        resp.sendError(500, "No asset server");
      }
    }
  }
}
