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

import org.juzu.impl.spi.request.RequestBridge;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class PortletRequestBridge<Rq extends PortletRequest, Rs extends PortletResponse> implements RequestBridge
{

   /** . */
   protected final Rq request;
   
   /** . */
   protected final Rs response;

   /** . */
   protected final String methodId;

   /** . */
   protected final Map<String, String[]> parameters;

   PortletRequestBridge(Rq request, Rs response)
   {
      Map<String, String[]> parameters = request.getParameterMap();
      String methodId= request.getParameter("op");
      if (methodId != null)
      {
         parameters = new HashMap<String, String[]>(parameters);
         parameters.remove("op");
      }


      //
      this.request = request;
      this.response = response;
      this.methodId = methodId;
      this.parameters = parameters;
   }

   public String getMethodId()
   {
      return methodId;
   }

   public final Map<String, String[]> getParameters()
   {
      return parameters;
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
      Map<Object, Object> store = (Map<Object, Object>)request.getAttribute("org.juzu.request_scope");
      if (store == null)
      {
         request.setAttribute("org.juzu.request_scope", store = new HashMap<Object, Object>());
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

   private Map<Object, Object> getSessionContext()
   {
      PortletSession session = request.getPortletSession();
      Map<Object, Object> store = (Map<Object, Object>)session.getAttribute("org.juzu.session_scope");
      if (store == null)
      {
         session.setAttribute("org.juzu.session_scope", store = new HashMap<Object, Object>());
      }
      return store;
   }

   private Map<Object, Object> getFlashContext()
   {
      PortletSession session = request.getPortletSession();
      Map<Object, Object> store = (Map<Object, Object>)session.getAttribute("org.juzu.flash_scope");
      if (store == null)
      {
         session.setAttribute("org.juzu.flash_scope", store = new HashMap<Object, Object>());
      }
      return store;
   }

   public Object getIdentityValue(Object key)
   {
      return null;
   }

   public void setIdentityValue(Object key, Object value)
   {
   }
}
