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

package org.juzu.portlet;

import org.juzu.Response;
import org.juzu.impl.request.ActionBridge;
import org.juzu.metadata.ControllerMethod;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class PortletActionBridge extends PortletRequestBridge<ActionRequest, ActionResponse> implements ActionBridge
{

   /** . */
   private ResponseImpl ri;

   PortletActionBridge(ActionRequest actionRequest, ActionResponse actionResponse)
   {
      super(actionRequest, actionResponse);

      //
      this.ri = null;
   }

   public Response createResponse(ControllerMethod method)
   {
      if (ri == null)
      {
         super.response.setRenderParameter("op", method.getId());
         ri = new ResponseImpl(super.response);
      }
      return ri;
   }

   public void redirect(String location) throws IOException
   {
      response.sendRedirect(location);
   }
}
