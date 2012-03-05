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
import org.w3c.dom.Element;

import javax.portlet.MimeResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.util.Collection;

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

         // For now only in gatein since liferay won't support it very well
         if (request.getPortalContext().getPortalInfo().startsWith("GateIn Portlet Container"))
         {
            Collection<String> stylesheets = render.getStylesheets();
            if (stylesheets.size() > 0)
            {
               for (String stylesheet : stylesheets)
               {
                  int pos = stylesheet.lastIndexOf('.');
                  String ext = pos == -1 ? "css" : stylesheet.substring(pos + 1);
                  Element elt = this.response.createElement("link");
                  elt.setAttribute("media", "screen");
                  elt.setAttribute("rel", "stylesheet");
                  elt.setAttribute("type", "text/" + ext);
                  elt.setAttribute("href", stylesheet);
                  this.response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, elt);
               }
            }

            Collection<String> scripts = render.getScripts();
            if (scripts.size() > 0)
            {
               for (String script : scripts)
               {
                  Element elt = this.response.createElement("script");
                  elt.setAttribute("type", "text/javascript");
                  elt.setAttribute("src", script);
                  this.response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, elt);
               }
            }
         }

         //
         String title = render.getTitle();
         if (title != null)
         {
            setTitle(title);
         }
      }
   }
}
