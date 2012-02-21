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
public abstract class ServletRequestBridge<R extends Response> implements RequestBridge<R>, HttpContext, WindowContext
{
   
   public static ServletRequestBridge<?> create(HttpServletRequest req, HttpServletResponse resp)
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

   public String getNamespace()
   {
      return "window_ns";
   }

   public String getId()
   {
      return "window_id";
   }
   //

   public Map<String, String[]> getParameters()
   {
      return parameters;
   }

   public Object getRequestValue(Object key)
   {
      return getRequestContext().get(key);
   }

   public void setRequestValue(Object key, Object value)
   {
      if (value != null)
      {
         getRequestContext().remove(key);
      }
      else
      {
         getRequestContext().put(key, value);
      }
   }

   private Map<Object, Object> getRequestContext()
   {
      Map<Object, Object> store = (Map<Object, Object>)req.getAttribute("org.juzu.request_scope");
      if (store == null)
      {
         req.setAttribute("org.juzu.request_scope", store = new HashMap<Object, Object>());
      }
      return store;
   }

   public void setSessionValue(Object key, Object value)
   {
      if (value == null)
      {
         getSessionContext().remove(key);
      }
      else
      {
         getSessionContext().put(key, value);
      }
   }

   public Object getSessionValue(Object key)
   {
      return getSessionContext().get(key);
   }

   public HttpContext getHttpContext()
   {
      return this;
   }

   public WindowContext getWindowContext()
   {
      return this;
   }

   public SecurityContext getSecurityContext()
   {
      return null;
   }

   public void setFlashValue(Object key, Object value)
   {
      if (value == null)
      {
         getFlashContext().remove(key);
      }
      else
      {
         getFlashContext().put(key, value);
      }
   }

   public Object getFlashValue(Object key)
   {
      Map<Object, Object> flash = getFlashContext();
      return flash != null ? flash.get(key) : null;
   }

   public Object getIdentityValue(Object key)
   {
      return null;
   }

   public void setIdentityValue(Object key, Object value)
   {
   }

   public void setResponse(R response) throws IllegalStateException, IOException
   {
      throw new UnsupportedOperationException("todo");
   }

   private Map<Object, Object> getSessionContext()
   {
      HttpSession session = req.getSession();
      Map<Object, Object> store = (Map<Object, Object>)session.getAttribute("org.juzu.session_scope");
      if (store == null)
      {
         session.setAttribute("org.juzu.session_scope", store = new HashMap<Object, Object>());
      }
      return store;
   }

   private Map<Object, Object> getFlashContext()
   {
      HttpSession session = req.getSession();
      Map<Object, Object> store = (Map<Object, Object>)session.getAttribute("org.juzu.flash_scope");
      if (store == null)
      {
         session.setAttribute("org.juzu.flash_scope", store = new HashMap<Object, Object>());
      }
      return store;
   }

   public String renderURL(Phase phase, Boolean escapeXML, Map<String, String[]> parameters)
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
}
