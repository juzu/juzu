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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ServletRequestBridge<R extends Response> implements RequestBridge<R>, HttpContext
{
   
   public static ServletRequestBridge<?> create(HttpServletRequest req, HttpServletResponse resp)
   {
      String methodId = null;
      Phase phase = Phase.RENDER;
      Map<String, String[]> parameters = new HashMap<String, String[]>();
      for (Map.Entry<String, String[]> entry : ((Map<String, String[]>)req.getParameterMap()).entrySet())
      {
         String name = entry.getKey();
         String[] value = entry.getValue();
         if (name.startsWith("p."))
         {
            parameters.put(name.substring(2), value);
         }
         else if ("phase".equals(name))
         {
            phase = Phase.valueOf(value[0]);
         }
         else if ("op".equals(name))
         {
            // For now like that
            methodId = value[0];
         }
      }
      
      //
      switch (phase)
      {
         case RENDER:
            return new ServletRenderBridge(req, resp, methodId, parameters);
         case ACTION:
            return new ServletActionBridge(req, resp, methodId, parameters);
         case RESOURCE:
            return new ServletResourceBridge(req, resp, methodId, parameters);
         default:
            throw new UnsupportedOperationException("todo");
      }
   }

   /** . */
   final String methodId;

   /** . */
   final HttpServletRequest req;

   /** . */
   final HttpServletResponse resp;

   /** . */
   final Map<String, String[]> parameters;

   ServletRequestBridge(
      HttpServletRequest req, 
      HttpServletResponse resp, 
      String methodId, 
      Map<String, String[]> parameters)
   {
      this.methodId = methodId;
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

   public String getMethodId()
   {
      return methodId;
   }

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
}
