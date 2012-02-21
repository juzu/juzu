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

package org.juzu.impl.spi.request.servlet;

import org.juzu.Response;
import org.juzu.impl.spi.request.RenderBridge;
import org.juzu.impl.spi.request.ResourceBridge;
import org.juzu.text.WriterPrinter;

import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletResourceBridge extends ServletMimeBridge<Response.Mime.Resource> implements ResourceBridge
{
   ServletResourceBridge(HttpServletRequest req, HttpServletResponse resp, Map<String, String[]> parameters)
   {
      super(req, resp, parameters);
   }

   public void setResponse(Response.Resource response) throws IllegalStateException, IOException
   {
      if (response instanceof Response.Mime.Resource)
      {
         Response.Mime.Resource resource = (Response.Mime.Resource)response;
         int status = resource.getStatus();
         if (status != 200)
         {
            resp.setStatus(status);
         }
      }

      //
      resp.setContentType("text/html");

      //
      PrintWriter writer = resp.getWriter();

      // Send response
      response.send(new WriterPrinter(writer));
   }
}
