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

package juzu.impl.bridge.spi.servlet;

import juzu.Response;
import juzu.impl.common.MimeType;
import juzu.impl.plugin.application.ApplicationContext;
import juzu.impl.bridge.spi.ActionBridge;
import juzu.impl.request.Method;
import juzu.request.ClientContext;
import juzu.impl.bridge.spi.DispatchSPI;
import juzu.request.Phase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletActionBridge extends ServletRequestBridge implements ActionBridge {

  /** . */
  Response response;

  ServletActionBridge(
      ApplicationContext application,
      Handler handler,
      HttpServletRequest req,
      HttpServletResponse resp,
      Method<?> target,
      Map<String, String[]> parameters) {
    super(application, handler, req, resp, target, parameters);
  }

  public ClientContext getClientContext() {
    return this;
  }

  public void setResponse(Response response) throws IllegalStateException, IOException {
    super.setResponse(response);
    if (response instanceof Response.View || response instanceof Response.Redirect) {
      this.response = response;
    } else {
      throw new IllegalArgumentException("Cannot accept response " + response);
    }
  }

  @Override
  void send() throws IOException {
    if (response instanceof Response.View) {
      Response.View update = (Response.View)response;
      DispatchSPI spi = createDispatch(Phase.VIEW, update.getTarget(), update.getParameters());
      Phase.View.Dispatch dispatch = new Phase.View.Dispatch(spi);
      String url = dispatch.with(MimeType.PLAIN).with(update.getProperties()).toString();
      for (Map.Entry<String, String[]> entry : responseHeaders.entrySet()) {
        resp.setHeader(entry.getKey(), entry.getValue()[0]);
      }
      resp.sendRedirect(url);
    }
    else if (response instanceof Response.Redirect) {
      Response.Redirect redirect = (Response.Redirect)response;
      String url = redirect.getLocation();
      resp.sendRedirect(url);
    }
  }
}
