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

package org.sample.booking.portlet;

import org.w3c.dom.Element;

import javax.portlet.MimeResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.RenderFilter;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class HeaderFilter implements RenderFilter
{

   public void init(FilterConfig filterConfig) throws PortletException
   {
   }

   public void doFilter(RenderRequest request, RenderResponse response, FilterChain chain) throws IOException, PortletException
   {
      if (request.getPortalContext().getPortalInfo().startsWith("GateIn Portlet Container"))
      {
         Element jQuery1 = response.createElement("script");
         jQuery1.setAttribute("type", "text/javascript");
         jQuery1.setAttribute("src", request.getContextPath() + "/public/javascripts/jquery-1.7.1.min.js");
         response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, jQuery1);

         //
         Element jQuery2 = response.createElement("script");
         jQuery2.setAttribute("type", "text/javascript");
         jQuery2.setAttribute("src", request.getContextPath() + "/public/javascripts/jquery-ui-1.7.2.custom.min.js");
         response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, jQuery2);

         //
         Element jQuery3 = response.createElement("script");
         jQuery3.setAttribute("type", "text/javascript");
         jQuery3.setAttribute("src", request.getContextPath() + "/public/javascripts/booking.js");
         response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, jQuery3);

         //
         Element css1 = response.createElement("link");
         css1.setAttribute("rel", "stylesheet");
         css1.setAttribute("type", "text/css");
         css1.setAttribute("media", "screen");
         css1.setAttribute("href", request.getContextPath() + "/public/stylesheets/main.css");
         response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, css1);

         //
         Element css2 = response.createElement("link");
         css2.setAttribute("rel", "stylesheet");
         css2.setAttribute("type", "text/css");
         css2.setAttribute("media", "screen");
         css2.setAttribute("href", request.getContextPath() + "/public/ui-lightness/jquery-ui-1.7.2.custom.css");
         response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, css2);
      }

      //
      chain.doFilter(request, response);
   }

   public void destroy()
   {
   }
}
