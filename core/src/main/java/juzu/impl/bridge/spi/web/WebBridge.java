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

package juzu.impl.bridge.spi.web;

import juzu.impl.bridge.spi.ScopedContext;
import juzu.request.ApplicationContext;
import juzu.request.ClientContext;
import juzu.request.HttpContext;
import juzu.request.UserContext;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class WebBridge {

  public abstract WebRequestContext getRequestContext();

  //

  public abstract void renderRequestURL(Appendable appendable) throws IOException;

  //

  public abstract ScopedContext getRequestScope(boolean create);

  public abstract ScopedContext getFlashScope(boolean create);

  public abstract ScopedContext getSessionScope(boolean create);

  public abstract void purgeSession();

  //

  public abstract HttpContext getHttpContext();

  public abstract ClientContext getClientContext();

  public abstract UserContext getUserContext();

  public abstract ApplicationContext getApplicationContext();

}
