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

import juzu.Response;
import juzu.impl.bridge.Bridge;
import juzu.impl.plugin.asset.AssetService;
import juzu.impl.request.ControllerHandler;
import juzu.request.Phase;
import juzu.request.RequestParameter;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class WebMimeBridge extends WebRequestBridge {

  WebMimeBridge(
      Bridge bridge,
      juzu.impl.bridge.spi.web.Handler handler,
      WebBridge http,
      Phase phase,
      ControllerHandler<?> target,
      Map<String, RequestParameter> parameters) {
    super(bridge, handler, http, phase, target, parameters);
  }

  @Override
  boolean send() throws Exception {
    if (super.send()) {
      return true;
    } else if (response instanceof Response.Status) {

      // For now we hardcode this
      Response.Status status = (Response.Status)response;

      //
      AssetService assetPlugin = (AssetService)handler.getBridge().getApplication().getPlugin("asset");

      //
      http.getRequestContext().send(assetPlugin, status, getPhase().name().equals(Phase.RESOURCE.name()));

      //
      return true;
    } else {
      return false;
    }
  }
}
