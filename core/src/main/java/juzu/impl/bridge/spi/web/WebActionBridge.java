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

import juzu.PropertyType;
import juzu.Response;
import juzu.impl.bridge.Bridge;
import juzu.impl.common.MimeType;
import juzu.impl.bridge.spi.ActionBridge;
import juzu.impl.request.Method;
import juzu.request.RequestParameter;
import juzu.request.ClientContext;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class WebActionBridge extends WebRequestBridge implements ActionBridge {

  WebActionBridge(
      Bridge bridge,
      Handler handler,
      WebBridge http,
      Method<?> target,
      Map<String, RequestParameter> parameters) {
    super(bridge, handler, http, target, parameters);
  }

  public ClientContext getClientContext() {
    return http.getClientContext();
  }

  @Override
  boolean send() throws IOException {
    if (super.send()) {
      return true;
    } else if (response instanceof Response.View) {
      Response.View update = (Response.View)response;
      String url = update.with(MimeType.PLAIN).with(update.getProperties()).toString();
      Iterable<Map.Entry<String, String[]>> headers = response.getProperties().getValues(PropertyType.HEADER);
      if (headers != null) {
        for (Map.Entry<String, String[]> entry : headers) {
          http.setHeader(entry.getKey(), entry.getValue()[0]);
        }
      }
      http.sendRedirect(url);
      return true;
    }
    else if (response instanceof Response.Redirect) {
      Response.Redirect redirect = (Response.Redirect)response;
      String url = redirect.getLocation();
      http.sendRedirect(url);
      return true;
    } else {
      return false;
    }
  }
}
