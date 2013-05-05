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

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.impl.asset.Asset;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.spi.MimeBridge;
import juzu.impl.request.Method;
import juzu.request.RequestParameter;
import juzu.io.Stream;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class WebMimeBridge extends WebRequestBridge implements MimeBridge {

  WebMimeBridge(
      Bridge bridge,
      Handler handler,
      WebBridge http,
      Method<?> target,
      Map<String, RequestParameter> parameters) {
    super(bridge, handler, http, target, parameters);
  }

  @Override
  boolean send() throws IOException {
    if (super.send()) {
      return true;
    } else if (response instanceof Response.Content<?>) {

      // For now we hardcode this
      Response.Content<Stream.Char> content = (Response.Content<Stream.Char>)response;
      PropertyMap properties = response.getProperties();

      // Resolve stylesheets Asset -> Asset.Value
      Iterable<String> stylesheets = properties.getValues(PropertyType.STYLESHEET);
      if (stylesheets != null) {
        Iterable<Asset> stylesheetValues =  handler.getBridge().application.getStylesheetManager().resolveAssets(stylesheets);
        properties.setValues(WebBridge.STYLESHEET, stylesheetValues);
      }

      // Resolve scripts Asset -> Asset.Value
      Iterable<String> scripts = properties.getValues(PropertyType.SCRIPT);
      if (scripts != null) {
        Iterable<Asset> scriptValues = handler.getBridge().application.getScriptManager().resolveAssets(scripts);
        properties.setValues(WebBridge.SCRIPT, scriptValues);
      }

      //
      http.send(content, this instanceof WebRenderBridge);

      //
      return true;
    } else {
      return false;
    }
  }
}
