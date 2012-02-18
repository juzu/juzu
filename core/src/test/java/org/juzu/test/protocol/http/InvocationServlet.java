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

import org.juzu.impl.spi.request.servlet.ServletRequestBridge;
import org.juzu.impl.utils.Logger;
import org.juzu.request.ApplicationContext;
import org.juzu.test.AbstractHttpTestCase;
import org.juzu.test.protocol.mock.MockApplication;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class InvocationServlet extends HttpServlet
{

   /** . */
   private final Logger log = new Logger()
   {
      public void log(CharSequence msg)
      {
         System.out.print(msg);
      }

      public void log(CharSequence msg, Throwable t)
      {
         System.err.println(msg);
         t.printStackTrace(System.err);
      }
   };

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      MockApplication<?> application = AbstractHttpTestCase.getApplication();
      if (application == null)
      {
         resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No application");
      }
      else
      {
         ServletRequestBridge bridge = ServletRequestBridge.create(req, resp);
         ApplicationContext context = application.getContext();
         context.invoke(bridge);
      }
   }
}
