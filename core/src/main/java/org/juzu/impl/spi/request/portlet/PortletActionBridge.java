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
import org.juzu.portlet.JuzuPortlet;
import org.juzu.request.RenderContext;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletActionBridge extends PortletRequestBridge<ActionRequest, ActionResponse> implements ActionBridge
{

   /** . */
   private boolean done;

   public PortletActionBridge(PortletBridgeContext context, ActionRequest actionRequest, ActionResponse actionResponse, boolean prod)
   {
      super(context, actionRequest, actionResponse, prod);

      //
      this.done = false;
   }

   public void end(Response response) throws IllegalStateException, IOException
   {
      if (done)
      {
         throw new IllegalStateException();
      }
      if (response instanceof Response.Update)
      {
         Response.Update update = (Response.Update)response;

         // Parameters
         for (Map.Entry<String, String[]> entry : update.getParameters().entrySet())
         {
            super.resp.setRenderParameter(entry.getKey(), entry.getValue());
         }

         // Method id
         String methodId = update.getProperties().getValue(RenderContext.METHOD_ID);
         if (methodId != null)
         {
            super.resp.setRenderParameter("juzu.op", methodId);
         }

         //
         PortletMode portletMode = update.getProperties().getValue(JuzuPortlet.PORTLET_MODE);
         if (portletMode != null)
         {
            try
            {
               super.resp.setPortletMode(portletMode);
            }
            catch (PortletModeException e)
            {
               throw new IllegalArgumentException(e);
            }
         }

         //
         WindowState windowState = update.getProperties().getValue(JuzuPortlet.WINDOW_STATE);
         if (windowState != null)
         {
            try
            {
               super.resp.setWindowState(windowState);
            }
            catch (WindowStateException e)
            {
               throw new IllegalArgumentException(e);
            }
         }
      }
      else if (response instanceof Response.Redirect)
      {
         Response.Redirect redirect = (Response.Redirect)response;
         super.resp.sendRedirect(redirect.getLocation());
      }
      done = true;
   }
}
