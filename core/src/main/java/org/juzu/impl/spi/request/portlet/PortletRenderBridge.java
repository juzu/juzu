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

package org.juzu.impl.spi.request.portlet;

import org.juzu.Response;
import org.juzu.impl.spi.request.RenderBridge;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletRenderBridge extends PortletMimeBridge<RenderRequest, RenderResponse> implements RenderBridge
{

   public PortletRenderBridge(RenderRequest request, RenderResponse response, boolean buffer) throws IOException
   {
      super(request, response, buffer);
   }

   public void setTitle(String title)
   {
      response.setTitle(title);
   }

   @Override
   public void setResponse(Response response) throws IllegalStateException, IOException
   {
      super.setResponse(response);

      // Improve that because it will not work on streaming portals...
      // for now it's OK
      if (response instanceof Response.Content.Render)
      {
         Response.Content.Render render = (Response.Content.Render)response;
         String title = render.getTitle();
         if (title != null)
         {
            setTitle(title);
         }
      }
   }
}
