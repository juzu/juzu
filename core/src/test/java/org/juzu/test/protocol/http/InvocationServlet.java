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

import org.juzu.impl.asset.Registration;
import org.juzu.impl.asset.Router;
import org.juzu.impl.asset.Server;
import org.juzu.impl.spi.request.servlet.ServletRequestBridge;
import org.juzu.impl.utils.Logger;
import org.juzu.impl.utils.Tools;
import org.juzu.request.ApplicationContext;
import org.juzu.test.AbstractHttpTestCase;
import org.juzu.test.protocol.mock.MockApplication;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class InvocationServlet extends HttpServlet
{

   /**
    * Returns an asset server associated with the specified context or null if it does not exist.
    *
    * @param context the related context
    * @return the related server
    * @throws NullPointerException if the context argument is null
    */
   public static Server getServer(ServletContext context) throws NullPointerException
   {
      if (context == null)
      {
         throw new NullPointerException("No null context accepted");
      }
      return registry.get(context.getContextPath());
   }

   /** . */
   private static final ConcurrentHashMap<String, Server> registry = new ConcurrentHashMap<String, Server>();

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

   /** . */
   private MockApplication<?> application;

   @Override
   public void init() throws ServletException
   {
      try
      {
         MockApplication<?> application = AbstractHttpTestCase.getApplication();

         //
         Server server = (Server)getServletContext().getAttribute("asset.server");
         Registration<Router> app = server.getApplicationRouter().register(application.getDescriptor().getName(), Router.class);

         //
         application.bindBean(Router.class, Collections.<Annotation>singleton(Server.APPLICATION), app.getRoute());
         application.bindBean(Router.class, Collections.<Annotation>singleton(Server.PLUGIN), server.getPluginRouter());

         //
         application.init();

         //
         this.application = application;
      }
      catch (Exception e)
      {
         throw new ServletException(e);
      }
   }

   @Override
   public void destroy()
   {
      getServletContext().removeAttribute("asset.server");
   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      String s = req.getRequestURI().substring(req.getContextPath().length());
      if ("/jquery.js".equals(s))
      {
         resp.setContentType("text/javascript");
         OutputStream out = resp.getOutputStream();
         InputStream in = getServletContext().getResourceAsStream("/jquery.js");
         Tools.copy(in, out);
      }
      else
      {
         ServletRequestBridge bridge = ServletRequestBridge.create(req, resp);
         ApplicationContext context = application.getContext();
         context.invoke(bridge);
      }
   }
}
