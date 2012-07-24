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
        server.doGet(path, getServletContext(), resp);
      }
      else {
        resp.sendError(500, "No asset server");
      }
    }
  }
}
