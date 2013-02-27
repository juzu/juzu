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

package juzu.impl.bridge.spi.web;

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.impl.asset.Asset;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.spi.MimeBridge;
import juzu.impl.request.Method;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class WebMimeBridge extends WebRequestBridge implements MimeBridge {

  WebMimeBridge(
      Bridge bridge,
      Handler handler,
      WebBridge http,
      Method<?> target,
      Map<String, String[]> parameters) {
    super(bridge, handler, http, target, parameters);
  }

  @Override
  boolean send() throws IOException {
    if (super.send()) {
      return true;
    } else if (response instanceof Response.Content<?>) {

      //
      Response.Content<?> content = (Response.Content)response;
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
