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
package bridge.response.async.view;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;

/** @author Julien Viet */
@WebListener
public class CurrentRequest implements ServletRequestListener {

  /** . */
  static final ThreadLocal<HttpServletRequest> req = new ThreadLocal<HttpServletRequest>();

  public void requestInitialized(ServletRequestEvent sre) {
    req.set((HttpServletRequest)sre.getServletRequest());
  }

  public void requestDestroyed(ServletRequestEvent sre) {
    req.remove();
  }
}
