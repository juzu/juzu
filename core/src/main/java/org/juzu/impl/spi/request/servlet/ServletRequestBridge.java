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
import org.juzu.impl.inject.Scoped;
import org.juzu.impl.inject.ScopedContext;
import org.juzu.impl.spi.request.RequestBridge;
import org.juzu.request.HttpContext;
import org.juzu.request.Phase;
import org.juzu.request.SecurityContext;
import org.juzu.request.WindowContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ServletRequestBridge implements RequestBridge, HttpContext, WindowContext
{
   
   public static ServletRequestBridge create(HttpServletRequest req, HttpServletResponse resp)
   {
      Phase phase = Phase.RENDER;
      Map<String, String[]> parameters = new HashMap<String, String[]>();
      for (Map.Entry<String, String[]> entry : ((Map<String, String[]>)req.getParameterMap()).entrySet())
      {
         String name = entry.getKey();
         String[] value = entry.getValue();
         if (name.equals("juzu.phase"))
         {
            phase = Phase.valueOf(value[0]);
         }
         else
         {
            parameters.put(name, value);
         }
      }
      
      //
      switch (phase)
      {
         case RENDER:
            return new ServletRenderBridge(req, resp, parameters);
         case ACTION:
            return new ServletActionBridge(req, resp, parameters);
         case RESOURCE:
            return new ServletResourceBridge(req, resp, parameters);
         default:
            throw new UnsupportedOperationException("todo");
      }
   }

   /** . */
   final HttpServletRequest req;

   /** . */
   final HttpServletResponse resp;

   /** . */
   final Map<String, String[]> parameters;

   ServletRequestBridge(
      HttpServletRequest req, 
      HttpServletResponse resp, 
      Map<String, String[]> parameters)
   {
      this.req = req;
      this.resp = resp;
      this.parameters = parameters;
   }
   
   //

   public Cookie[] getCookies()
   {
      return req.getCookies();
   }

   public String getScheme()
   {
      return req.getScheme();
   }

   public int getServerPort()
   {
      return req.getServerPort();
   }

   public String getServerName()
   {
      return req.getServerName();
   }

   public String getContextPath()
   {
      return req.getContextPath();
   }

   //

   public final String getNamespace()
   {
      return "window_ns";
   }

   public final String getId()
   {
      return "window_id";
   }
   //

   public final Map<String, String[]> getParameters()
   {
      return parameters;
   }

   public final HttpContext getHttpContext()
   {
      return this;
   }

   public final WindowContext getWindowContext()
   {
      return this;
   }

   public final SecurityContext getSecurityContext()
   {
      return null;
   }

   public final Scoped getRequestValue(Object key)
   {
      ScopedContext context = getRequestContext(false);
      return context != null ? context.get(key) : null;
   }

   public final void setRequestValue(Object key, Scoped value)
   {
      if (value != null)
      {
         ScopedContext context = getRequestContext(false);
         if (context != null)
         {
            context.set(key, null);
         }
      }
      else
      {
         getRequestContext(true).set(key, value);
      }
   }

   public final Scoped getFlashValue(Object key)
   {
      ScopedContext context = getFlashContext(false);
      return context != null ? context.get(key) : null;
   }

   public final void setFlashValue(Object key, Scoped value)
   {
      if (value == null)
      {
         ScopedContext context = getFlashContext(false);
         if (context != null)
         {
            context.set(key, null);
         }
      }
      else
      {
         getFlashContext(true).set(key, value);
      }
   }

   public final Scoped getSessionValue(Object key)
   {
      ScopedContext context = getSessionContext(false);
      return context != null ? context.get(key) : null;
   }

   public final void setSessionValue(Object key, Scoped value)
   {
      if (value == null)
      {
         ScopedContext context = getSessionContext(false);
         if (context != null)
         {
            context.set(key, null);
         }
      }
      else
      {
         getSessionContext(true).set(key, value);
      }
   }

   public final Scoped getIdentityValue(Object key)
   {
      return null;
   }

   public final void setIdentityValue(Object key, Scoped value)
   {
   }

   public void setResponse(Response response) throws IllegalStateException, IOException
   {
      throw new UnsupportedOperationException("todo");
   }

   public final String renderURL(Phase phase, Boolean escapeXML, Map<String, String[]> parameters)
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append(req.getScheme());
      buffer.append("://");
      buffer.append(req.getServerName());
      int port = req.getServerPort();
      if (port != 80)
      {
         buffer.append(':').append(port);
      }
      buffer.append(req.getContextPath());
      buffer.append(req.getServletPath());
      buffer.append("?juzu.phase=").append(phase);
      for (Map.Entry<String, String[]> parameter : parameters.entrySet())
      {
         String name = parameter.getKey();
         try
         {
            String encName = URLEncoder.encode(name, "UTF-8");
            for (String value : parameter.getValue())
            {
               String encValue = URLEncoder.encode(value, "UTF-8");
               buffer.append("&").append(encName).append('=').append(encValue);
            }
         }
         catch (UnsupportedEncodingException e)
         {
            // Should not happen
            throw new AssertionError(e);
         }
      }
      return buffer.toString();
   }

   public void close()
   {
      ScopedContext context = getRequestContext(false);
      if (context != null)
      {
         context.close();
      }
   }

   protected final ScopedContext getRequestContext(boolean create)
   {
      ScopedContext context = (ScopedContext)req.getAttribute("org.juzu.request_scope");
      if (context == null && create)
      {
         req.setAttribute("org.juzu.request_scope", context = new ScopedContext());
      }
      return context;
   }

   protected final ScopedContext getFlashContext(boolean create)
   {
      ScopedContext context = null;
      HttpSession session = req.getSession(create);
      if (session != null)
      {
         context = (ScopedContext)session.getAttribute("org.juzu.flash_scope");
         if (context == null && create)
         {
            session.setAttribute("org.juzu.flash_scope", context = new ScopedContext());
         }
      }
      return context;
   }

   protected final ScopedContext getSessionContext(boolean create)
   {
      ScopedContext context = null;
      HttpSession session = req.getSession(create);
      if (session != null)
      {
         context = (ScopedContext)session.getAttribute("org.juzu.session_scope");
         if (context == null && create)
         {
            session.setAttribute("org.juzu.session_scope", context = new ScopedContext());
         }
      }
      return context;
   }
}
