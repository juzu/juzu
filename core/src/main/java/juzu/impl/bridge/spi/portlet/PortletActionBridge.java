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

package juzu.impl.bridge.spi.portlet;

import juzu.Response;
import juzu.impl.plugin.application.Application;
import juzu.impl.bridge.spi.ActionBridge;
import juzu.request.ClientContext;
import juzu.request.Phase;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletActionBridge extends PortletInteractionBridge<ActionRequest, ActionResponse> implements ActionBridge {

  /** . */
  private final PortletClientContext clientContext;

  public PortletActionBridge(Application application, ActionRequest request, ActionResponse response, boolean prod) {
    super(application, request, response, prod);

    //
    this.clientContext = new PortletClientContext(request);
  }

  public ClientContext getClientContext() {
    return clientContext;
  }

  @Override
  protected Phase getPhase() {
    return Phase.ACTION;
  }

  public void setResponse(Response response) throws IllegalStateException, IOException {
    super.setResponse(response);
    if (response instanceof Response.Redirect) {
      Response.Redirect redirect = (Response.Redirect)response;
      super.resp.sendRedirect(redirect.getLocation());
    } else {
      super.setResponse(response);
    }
  }
}
