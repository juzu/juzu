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

package juzu.test.protocol.http;

import juzu.Response;
import juzu.impl.bridge.spi.ActionBridge;
import juzu.impl.common.Logger;
import juzu.impl.common.MethodHandle;
import juzu.impl.plugin.application.ApplicationLifeCycle;
import juzu.request.ClientContext;
import juzu.impl.bridge.spi.DispatchSPI;
import juzu.request.Phase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ActionBridgeImpl extends RequestBridgeImpl implements ActionBridge {

  /** . */
  private Response response;

  ActionBridgeImpl(
      Logger log,
      ApplicationLifeCycle<?, ?> application,
      HttpServletRequest req,
      HttpServletResponse resp,
      MethodHandle target,
      Map<String, String[]> parameters) {
    super(log, application, req, resp, target, parameters);
  }

  public ClientContext getClientContext() {
    return this;
  }

  public void setResponse(Response response) throws IllegalStateException, IOException {
    if (response instanceof Response.View || response instanceof Response.Redirect) {
      this.response = response;
    } else {
      throw new IllegalArgumentException();
    }
  }

  public void close() {
    try {
      if (response instanceof Response.View) {
        Response.View update = (Response.View)response;
        DispatchSPI spi = createDispatch(Phase.VIEW, update.getTarget(), update.getParameters());
        Phase.View.Dispatch dispatch = new Phase.View.Dispatch(spi);
        String url = dispatch.with(update.getProperties()).toString();
        resp.sendRedirect(url);
      }
      else if (response instanceof Response.Redirect) {
        Response.Redirect redirect = (Response.Redirect)response;
        String url = redirect.getLocation();
        resp.sendRedirect(url);
      }
    }
    catch (IOException e) {
      // ?????
      e.printStackTrace();
    }
  }
}
