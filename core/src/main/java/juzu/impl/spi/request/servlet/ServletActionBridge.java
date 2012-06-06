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

package juzu.impl.spi.request.servlet;

import juzu.Response;
import juzu.impl.spi.request.ActionBridge;
import juzu.request.Phase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletActionBridge extends ServletRequestBridge implements ActionBridge {
  ServletActionBridge(
    ServletBridgeContext context,
    HttpServletRequest req,
    HttpServletResponse resp,
    String methodId,
    Map<String, String[]> parameters) {
    super(context, req, resp, methodId, parameters);
  }

  @Override
  public void end(Response response) throws IllegalStateException, IOException {
    if (response instanceof Response.Update) {
      Response.Update update = (Response.Update)response;
      String url = renderURL(Phase.RENDER, update.getParameters(), update.getProperties());
      resp.sendRedirect(url);
    }
    else if (response instanceof Response.Redirect) {
      Response.Redirect redirect = (Response.Redirect)response;
      String url = redirect.getLocation();
      resp.sendRedirect(url);
    }
  }
}
