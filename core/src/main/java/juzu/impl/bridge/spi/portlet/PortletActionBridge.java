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
import juzu.impl.bridge.spi.ActionBridge;
import juzu.request.ClientContext;
import juzu.request.Phase;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletActionBridge extends PortletInteractionBridge<ActionRequest, ActionResponse> implements ActionBridge {

  /** . */
  private final PortletClientContext clientContext;

  public PortletActionBridge(Bridge bridge, ActionRequest request, ActionResponse response, PortletConfig config) {
    super(bridge, request, response, config);

    //
    this.clientContext = new PortletClientContext(request);
  }

  public ClientContext getClientContext() {
    return clientContext;
  }

  @Override
  protected Phase getPhase() {
    return Phase.ACTION;
  }

  @Override
  public void send() throws IOException, PortletException {
    if (response instanceof Response.Redirect) {
      Response.Redirect redirect = (Response.Redirect)response;
      super.resp.sendRedirect(redirect.getLocation());
    } else {
      super.send();
    }
  }
}
