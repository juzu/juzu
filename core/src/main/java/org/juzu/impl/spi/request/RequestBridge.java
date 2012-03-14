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

package org.juzu.impl.spi.request;

import org.juzu.PropertyType;
import org.juzu.Response;
import org.juzu.impl.inject.Scoped;
import org.juzu.request.HttpContext;
import org.juzu.request.SecurityContext;
import org.juzu.request.WindowContext;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface RequestBridge
{

   /**
    * Returns the request parameters.
    *
    * @return the request parameters
    */
   Map<String, String[]> getParameters();

   <T> T getProperty(PropertyType<T> propertyType);

   Scoped getFlashValue(Object key);

   void setFlashValue(Object key, Scoped value);

   Scoped getRequestValue(Object key);

   void setRequestValue(Object key, Scoped value);

   Scoped getSessionValue(Object key);

   void setSessionValue(Object key, Scoped value);

   Scoped getIdentityValue(Object key);

   void setIdentityValue(Object key, Scoped value);

   HttpContext getHttpContext();

   SecurityContext getSecurityContext();
   
   WindowContext getWindowContext();

   void setResponse(Response response) throws IllegalStateException, IOException;
}
