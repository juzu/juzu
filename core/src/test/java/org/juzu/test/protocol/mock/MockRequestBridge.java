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

package org.juzu.test.protocol.mock;

import org.juzu.impl.inject.Scoped;
import org.juzu.impl.spi.request.RequestBridge;

import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MockRequestBridge implements RequestBridge
{

   /** . */
   protected final MockClient client;

   /** . */
   private final Map<String, String[]> parameters;

   /** . */
   private final Map<Object, Scoped> attributes;

   /** . */
   private final MockHttpContext httpContext;

   /** . */
   private final MockSecurityContext securityContext;

   /** . */
   private final MockWindowContext windowContext;

   public MockRequestBridge(MockClient client)
   {
      this.client = client;
      this.parameters = new HashMap<String, String[]>();
      this.attributes = new HashMap<Object, Scoped>();
      this.httpContext = new MockHttpContext();
      this.securityContext = new MockSecurityContext();
      this.windowContext = new MockWindowContext();
   }

   public Map<Object, Scoped> getAttributes()
   {
      return attributes;
   }

   public Map<String, String[]> getParameters()
   {
      return parameters;
   }

   public Scoped getFlashValue(Object key)
   {
      return client.getFlashValue(key);
   }

   public void setFlashValue(Object key, Scoped value)
   {
      client.setFlashValue(key, value);
   }

   public Scoped getRequestValue(Object key)
   {
      return attributes.get(key);
   }

   public void setRequestValue(Object key, Scoped value)
   {
      if (value != null)
      {
         attributes.put(key, value);
      }
      else
      {
         attributes.remove(key);
      }
   }

   public Scoped getSessionValue(Object key)
   {
      return client.getSession().get(key);
   }

   public void setSessionValue(Object key, Scoped value)
   {
      if (value != null)
      {
         client.getSession().put(key, value);
      }
      else
      {
         client.getSession().remove(key);
      }
   }

   public Scoped getIdentityValue(Object key)
   {
      return null;
   }

   public void setIdentityValue(Object key, Scoped value)
   {
   }

   public MockSecurityContext getSecurityContext()
   {
      return securityContext;
   }

   public MockHttpContext getHttpContext()
   {
      return httpContext;
   }

   public MockWindowContext getWindowContext()
   {
      return windowContext;
   }
}
