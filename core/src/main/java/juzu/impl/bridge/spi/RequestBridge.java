/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.bridge.spi;

import juzu.PropertyType;
import juzu.Response;
import juzu.impl.common.MethodHandle;
import juzu.impl.inject.Scoped;
import juzu.impl.request.Argument;
import juzu.impl.request.Request;
import juzu.request.ApplicationContext;
import juzu.request.HttpContext;
import juzu.request.Phase;
import juzu.request.SecurityContext;
import juzu.request.UserContext;
import juzu.request.WindowContext;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface RequestBridge extends Closeable {

  /**
   * Returns the request target.
   *
   * @return the request target
   */
  MethodHandle getTarget();

  /**
   * Returns the request arguments.
   *
   * @return the request arguments.
   */
  Map<String, ? extends Argument> getArguments();

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

  UserContext getUserContext();

  ApplicationContext getApplicationContext();

  /**
   * todo: see if we can remove that and instead do it from the session context directly.
   */
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

  /**
   * Create a dispatch for the specified phase, target and parameters.
   *
   * @param phase the dispatch phase
   * @param target the dispatch target
   * @param parameters the dispatch parameters
   * @return the dispatch object
   * @throws IllegalArgumentException if any parameter is not valid
   * @throws NullPointerException if any argument is null
   */
  DispatchSPI createDispatch(Phase phase, MethodHandle target, Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException;
}
