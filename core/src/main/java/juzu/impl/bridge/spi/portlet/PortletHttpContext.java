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

package juzu.impl.bridge.spi.portlet;

import juzu.Method;
import juzu.request.HttpContext;

import javax.portlet.ClientDataRequest;
import javax.portlet.PortletRequest;
import javax.servlet.http.Cookie;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletHttpContext implements HttpContext {

  /** . */
  private final PortletRequest request;

  /** . */
  private final Method method;

  public PortletHttpContext(PortletRequest request) {
    this.request = request;
    this.method = request instanceof ClientDataRequest ? Method.valueOf(((ClientDataRequest)request).getMethod()) : Method.GET;
  }

  public Method getMethod() {
    return method;
  }

  public Cookie[] getCookies() {
    return request.getCookies();
  }

  public String getScheme() {
    return request.getScheme();
  }

  public int getServerPort() {
    return request.getServerPort();
  }

  public String getServerName() {
    return request.getServerName();
  }

  public String getContextPath() {
    return request.getContextPath();
  }
}
