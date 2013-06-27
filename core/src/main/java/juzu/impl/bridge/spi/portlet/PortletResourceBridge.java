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

import juzu.PropertyType;
import juzu.Response;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.spi.ResourceBridge;
import juzu.request.ClientContext;
import juzu.request.Phase;

import javax.portlet.PortletConfig;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletResourceBridge extends PortletMimeBridge<ResourceRequest, ResourceResponse> implements ResourceBridge {

  /** . */
  private final PortletClientContext clientContext;

  public PortletResourceBridge(Bridge bridge, ResourceRequest request, ResourceResponse response, PortletConfig config) {
    super(bridge, request, response, config);

    //
    this.clientContext = new PortletClientContext(request);
  }

  @Override
  protected Phase getPhase() {
    return Phase.RESOURCE;
  }

  public ClientContext getClientContext() {
    return clientContext;
  }

  @Override
  protected void sendProperties() throws IOException {
    if (response instanceof Response.Content) {
      Response.Content resource = (Response.Content)response;
      int status = resource.getStatus();
      if (status != 200) {
        resp.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(status));
      }
      Iterable<Map.Entry<String, String[]>> headers = resource.getProperties().getValues(PropertyType.HEADER);
      if (headers != null) {
        for (Map.Entry<String, String[]> header : headers) {
          String[] values = header.getValue();
          if (values.length > 0) {
            resp.setProperty(header.getKey(), values[0]);
          }
        }
      }
      super.setResponse(response);
    }
  }
}
