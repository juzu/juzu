/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package juzu.impl.bridge.spi;

import juzu.PropertyType;
import juzu.Response;
import juzu.impl.common.MethodHandle;
import juzu.impl.inject.Scoped;
import juzu.impl.request.Request;
import juzu.request.HttpContext;
import juzu.request.SecurityContext;
import juzu.request.WindowContext;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface RequestBridge {

  MethodHandle getTarget();

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

  void purgeSession();

  /**
   * Set the specified response on the bridge.
   *
   * @param response the response
   * @throws IllegalArgumentException if the response cannot be honoured
   * @throws IOException              any io exception
   */
  void setResponse(Response response) throws IllegalArgumentException, IOException;

  /**
   * Signals the beginning of a request.
   *
   * @param request the request
   */
  void begin(Request request);

  /**
   * Signals the end of a request. During this time, the request bridge should terminate any activity
   * in relation with the request (such as closing opened scoped context).
   */
  void end();

  /**
   * Terminates the life cycle of the request bridge.
   */
  void close();

}
