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
import org.juzu.text.WriterPrinter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletRenderBridge extends ServletMimeBridge implements RenderBridge
{
   ServletRenderBridge(HttpServletRequest req, HttpServletResponse resp, Map<String, String[]> parameters)
   {
      super(req, resp, parameters);
   }

   public void setTitle(String title)
   {
   }

   @Override
   public void setResponse(Response response) throws IllegalStateException, IOException
   {
      Response.Content.Render render = (Response.Render)response;
      resp.setContentType("text/html");

      //
      PrintWriter writer = resp.getWriter();

      //
      writer.println("<!DOCTYPE html>");
      writer.println("<html>");
      
      writer.println("<head>");
      Collection<String> scripts = render.getScripts();
      if (scripts.size() > 0)
      {
         for (String script : scripts)
         {
            writer.print("<script type=\"text/javascript\" src=\"");
            writer.print(script);
            writer.println("\"></script>");
         }
      }
      Collection<String> stylesheets = render.getStylesheets();
      if (stylesheets.size() > 0)
      {
         for (String stylesheet : stylesheets)
         {
            int pos = stylesheet.lastIndexOf('.');
            String ext = pos == -1 ? "css" : stylesheet.substring(pos + 1);
            writer.print("<link rel=\"stylesheet\" type=\"text/");
            writer.print(ext);
            writer.print("\" href=\"");
            writer.print(stylesheet);
            writer.println("\"></link>");
         }
      }
      writer.println("</head>");
      
      //
      writer.println("<body>");

      // Send response
      render.send(new WriterPrinter(writer));

      //
      writer.println("</body>");
      writer.println("</html>");
   }
}
