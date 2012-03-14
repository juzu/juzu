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
import org.juzu.impl.spi.request.ActionBridge;
import org.juzu.request.RenderContext;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletActionBridge extends PortletRequestBridge<ActionRequest, ActionResponse> implements ActionBridge
{

   /** . */
   private boolean done;

   public PortletActionBridge(ActionRequest actionRequest, ActionResponse actionResponse)
   {
      super(actionRequest, actionResponse);

      //
      this.done = false;
   }

   public void setResponse(Response response) throws IllegalStateException, IOException
   {
      if (done)
      {
         throw new IllegalStateException();
      }
      if (response instanceof Response.Update)
      {
         done = true;
         Response.Update update = (Response.Update)response;
         for (Map.Entry<String, String[]> entry : update.getParameters().entrySet())
         {
            super.response.setRenderParameter(entry.getKey(), entry.getValue());
         }
         Object methodId = update.getProperties().get(RenderContext.METHOD_ID);
         if (methodId != null)
         {
            super.response.setRenderParameter("juzu.op", methodId.toString());
         }
      }
      else
      {
         done = true;
         Response.Redirect redirect = (Response.Redirect)response;
         super.response.sendRedirect(redirect.getLocation());
      }
   }
}
