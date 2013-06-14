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

import juzu.Response;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.spi.ResourceBridge;
import juzu.impl.common.Tools;
import juzu.io.OutputStream;
import juzu.io.Stream;
import juzu.request.ClientContext;
import juzu.request.Phase;

import javax.portlet.PortletConfig;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;
import java.nio.charset.Charset;

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
  public Stream createStream(String mimeType, Charset charset) throws IOException {
    if (mimeType != null) {
      resp.setContentType(mimeType);
    }

    //
    if (charset == null) {
      // We use ISO-8859-1 in case the the developer has not set a charset
      // but it will send chars instead of bytes
      charset = Tools.ISO_8859_1;
    } else {
      resp.setCharacterEncoding(charset.name());
    }

    //
    return OutputStream.create(charset, this.resp.getPortletOutputStream());
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
      int status = resource.getCode();
      if (status != 200) {
        resp.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(status));
      }
      super.setResponse(response);
    }
  }
}
