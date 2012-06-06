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

package juzu.impl.spi.request.portlet;

import juzu.Response;
import juzu.impl.spi.request.ResourceBridge;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletResourceBridge extends PortletMimeBridge<ResourceRequest, ResourceResponse> implements ResourceBridge {

  public PortletResourceBridge(PortletBridgeContext context, ResourceRequest request, ResourceResponse response, boolean buffer, boolean prod) {
    super(context, request, response, buffer, prod);
  }

  @Override
  public void end(Response response) throws IllegalStateException, IOException {
    if (response instanceof Response.Content.Resource) {
      Response.Content.Resource resource = (Response.Content.Resource)response;
      int status = resource.getStatus();
      if (status != 200) {
        this.resp.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(status));
      }
    }

    //
    super.end(response);
  }
}
