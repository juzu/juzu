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

import org.juzu.PropertyMap;
import org.juzu.PropertyType;
import org.juzu.URLBuilder;
import org.juzu.impl.inject.Scoped;
import org.juzu.impl.inject.ScopedContext;
import org.juzu.impl.spi.request.RequestBridge;
import org.juzu.impl.utils.JSON;
import org.juzu.impl.utils.Tools;
import org.juzu.request.Phase;
import org.juzu.request.RequestContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MockRequestBridge implements RequestBridge
{

   /** . */
   protected final MockClient client;

   /** . */
   private final String methodId;

   /** . */
   private final Map<String, String[]> parameters;

   /** . */
   private final ScopedContext attributes;

   /** . */
   private final MockHttpContext httpContext;

   /** . */
   private final MockSecurityContext securityContext;

   /** . */
   private final MockWindowContext windowContext;

   /** . */
   private final List<Scoped> attributesHistory;

   public MockRequestBridge(MockClient client, String methodId, Map<String, String[]> parameters)
   {
      this.client = client;
      this.methodId = methodId;
      this.parameters = parameters;
      this.attributes = new ScopedContext();
      this.httpContext = new MockHttpContext();
      this.securityContext = new MockSecurityContext();
      this.windowContext = new MockWindowContext();
      this.attributesHistory = new ArrayList<Scoped>();
   }

   public List<Scoped> getAttributesHistory()
   {
      return attributesHistory;
   }

   public <T> T getProperty(PropertyType<T> propertyType)
   {
      if (RequestContext.METHOD_ID.equals(propertyType))
      {
         return propertyType.getType().cast(methodId);
      }
      return null;
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
         attributes.set(key, value);
      }
      else
      {
         attributes.set(key, null);
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
         client.getSession().set(key, value);
      }
      else
      {
         client.getSession().set(key, null);
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

   void close()
   {
      attributesHistory.addAll(Tools.list(attributes));
      attributes.close();
   }

   public <T> String checkPropertyValidity(Phase phase, PropertyType<T> propertyType, T propertyValue)
   {
      return _checkPropertyValidity(phase, propertyType, propertyValue);
   }

   public String _checkPropertyValidity(Phase phase, PropertyType<?> propertyType, Object propertyValue)
   {
      if (propertyType == URLBuilder.ESCAPE_XML)
      {
         // OK
         return null;
      }
      else if (propertyType == RequestContext.METHOD_ID)
      {
         // OK
         return null;
      }
      else
      {
         return "Unsupported property " + propertyType + " = " + propertyValue;
      }
   }

   public String renderURL(Phase phase, Map<String, String[]> parameters, PropertyMap properties)
   {
      JSON props = new JSON();
      if (properties != null)
      {
         for (PropertyType<?> property : properties)
         {
            Object value = properties.getValue(property);
            String valid = _checkPropertyValidity(phase, property, value);
            if (valid != null)
            {
               throw new IllegalArgumentException(valid);
            }
            else
            {
               props.set(property.getClass().getName(), value);
            }
         }
      }

      //
      JSON url = new JSON();
      url.set("phase", phase.name());
      url.map("parameters", parameters);
      url.set("properties", props);
      return url.toString();
   }
}
