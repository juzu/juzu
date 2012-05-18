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

package org.juzu.impl.spi.request.portlet;

import org.juzu.PropertyType;
import org.juzu.impl.inject.ScopedContext;
import org.juzu.impl.inject.Scoped;
import org.juzu.impl.request.Request;
import org.juzu.impl.spi.request.RequestBridge;
import org.juzu.portlet.JuzuPortlet;
import org.juzu.request.HttpContext;
import org.juzu.request.RequestContext;
import org.juzu.request.SecurityContext;
import org.juzu.request.WindowContext;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class PortletRequestBridge<Rq extends PortletRequest, Rs extends PortletResponse> implements RequestBridge
{

   /** . */
   protected final PortletBridgeContext context;

   /** . */
   protected final Rq req;
   
   /** . */
   protected final Rs resp;

   /** . */
   protected final String methodId;

   /** . */
   protected final Map<String, String[]> parameters;

   /** . */
   protected final PortletHttpContext httpContext;

   /** . */
   protected final PortletSecurityContext securityContext;

   /** . */
   protected final PortletWindowContext windowContext;

   /** . */
   protected Request request;

   PortletRequestBridge(PortletBridgeContext context, Rq req, Rs resp)
   {
      String methodId = null;
      Map<String, String[]> parameters = new HashMap<String, String[]>(req.getParameterMap());
      for (Iterator<Map.Entry<String, String[]>> i = parameters.entrySet().iterator();i.hasNext();)
      {
         Map.Entry<String, String[]> parameter = i.next();
         String key = parameter.getKey();
         if (key.startsWith("juzu."))
         {
            if (parameter.getKey().equals("juzu.op"))
            {
               methodId = parameter.getValue()[0];
            }
            i.remove();
         }
      }

      //
      this.context = context;
      this.req = req;
      this.resp = resp;
      this.methodId = methodId;
      this.parameters = parameters;
      this.httpContext = new PortletHttpContext(req);
      this.securityContext = new PortletSecurityContext(req);
      this.windowContext = new PortletWindowContext(this);
   }
   
   public <T> T getProperty(PropertyType<T> propertyType)
   {
      Object propertyValue = null;
      if (JuzuPortlet.PORTLET_MODE.equals(propertyType))
      {
         propertyValue = req.getPortletMode();
      }
      else if (JuzuPortlet.WINDOW_STATE.equals(propertyType))
      {
         propertyValue = req.getWindowState();
      }
      else if (RequestContext.METHOD_ID.equals(propertyType))
      {
         propertyValue = methodId;
      }
      return propertyValue == null ? null : propertyType.getType().cast(propertyValue);
   }

   public final Map<String, String[]> getParameters()
   {
      return parameters;
   }

   public final HttpContext getHttpContext()
   {
      return httpContext;
   }

   public final SecurityContext getSecurityContext()
   {
      return securityContext;
   }

   public final WindowContext getWindowContext()
   {
      return windowContext;
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
      PortletSession session = req.getPortletSession(create);
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
      PortletSession session = req.getPortletSession(create);
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

   public void begin(Request request)
   {
      this.request = request;
   }
}
