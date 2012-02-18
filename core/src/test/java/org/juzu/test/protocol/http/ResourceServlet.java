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

package org.juzu.test.protocol.http;

import org.juzu.impl.utils.Tools;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ResourceServlet extends HttpServlet
{

   /** . */
   private static final Map<String, String> contentTypes = new HashMap<String, String>();

   static
   {
      contentTypes.put("js", "application/javascript");
   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      ServletContext ctx = getServletContext();
      String uri = req.getRequestURI().substring(req.getContextPath().length() + 1);
      URL resource = resource = ctx.getResource(uri);
      if (resource == null)
      {
         resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
      else
      {
         String file = resource.getFile();
         int pos = file.lastIndexOf('.');
         String ext = pos != -1 ? file.substring(pos + 1) : "";
         String contentType = contentTypes.get(ext.trim().toLowerCase());
         if (contentType != null)
         {
            resp.setContentType(contentType);
         }
         InputStream in = resource.openStream();
         try
         {
            ServletOutputStream out = resp.getOutputStream();
            try
            {
               Tools.copy(in, out);
            }
            finally
            {
               Tools.safeClose(out);
            }
         }
         finally
         {
            Tools.safeClose(in);
         }
      }
   }
}
