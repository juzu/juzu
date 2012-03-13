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
import org.juzu.impl.spi.request.ResourceBridge;
import org.juzu.impl.utils.Tools;
import org.juzu.io.AppendableStream;
import org.juzu.io.BinaryOutputStream;
import org.juzu.io.CharStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletResourceBridge extends ServletMimeBridge implements ResourceBridge
{
   ServletResourceBridge(HttpServletRequest req, HttpServletResponse resp, Map<String, String[]> parameters)
   {
      super(req, resp, parameters);
   }

   public void setResponse(Response response) throws IllegalStateException, IOException
   {
      Response.Content content = (Response.Content)response;
      
      //
      if (content instanceof Response.Content.Resource)
      {
         Response.Content.Resource resource = (Response.Content.Resource)content;
         int status = resource.getStatus();
         if (status != 200)
         {
            resp.setStatus(status);
         }
      }

      // Set mime type
      String mimeType = content.getMimeType();
      if (mimeType != null)
      {
         resp.setContentType(mimeType);
      }

      // Send response
      if (content.getKind() == CharStream.class)
      {
         PrintWriter writer = resp.getWriter();
         try
         {
            ((Response.Resource)response).send(new AppendableStream(writer));
         }
         finally
         {
            Tools.safeClose(writer);
         }
      }
      else
      {
         OutputStream out = resp.getOutputStream();
         try
         {
            ((Response.Resource)response).send(new BinaryOutputStream(out));
         }
         finally
         {
            Tools.safeClose(out);
         }
      }
   }
}
