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

import juzu.EventQueue;
import juzu.Response;
import juzu.bridge.portlet.JuzuPortlet;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.Parameters;
import juzu.impl.request.ControlParameter;
import juzu.impl.request.ResponseParameter;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.Method;
import juzu.request.Phase;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletRequest;
import javax.portlet.StateAwareResponse;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class PortletInteractionBridge<Rq extends PortletRequest, Rs extends StateAwareResponse> extends PortletRequestBridge<Rq, Rs> {

  protected PortletInteractionBridge(Bridge bridge, Rq req, Rs resp, PortletConfig config) {
    super(bridge, req, resp, config);

    //
    init();
  }

  protected PortletInteractionBridge(Bridge bridge, Rq req, Rs resp, PortletConfig config, Method<?> target, Map<String, String[]> parameters) {
    super(bridge, req, resp, config, target, parameters);

    //
    init();
  }

  /**
   * Set event producer as a contextual argument.
   */
  private void init() {
    for (ControlParameter parameter : target.getParameters()) {
      if (parameter instanceof ContextualParameter) {
        final ContextualParameter contextualParameter = (ContextualParameter)parameter;
        if (EventQueue.class.isAssignableFrom(contextualParameter.getType())) {
          final PortletEventProducer producer = new PortletEventProducer();
          arguments.put(contextualParameter, producer);
        }
      }
    }
  }

  @Override
  public void send() throws IOException, PortletException {
    if (response instanceof Response.View) {
      Phase.View.Dispatch update = (Phase.View.Dispatch)response;

      // Parameters : need to remove that nasty cast
      Parameters parameters = (Parameters)update.getParameters();
      for (ResponseParameter entry : parameters.values()) {
        super.resp.setRenderParameter(entry.getName(), entry.toArray());
      }

      //
      Method method = bridge.application.getPlugin(ControllerPlugin.class).getDescriptor().getMethodByHandle(update.getTarget());

      // Method id
      super.resp.setRenderParameter("juzu.op", method.getId());

      //
      PortletMode portletMode = update.getProperties().getValue(JuzuPortlet.PORTLET_MODE);
      if (portletMode != null) {
        try {
          super.resp.setPortletMode(portletMode);
        }
        catch (PortletModeException e) {
          throw new IllegalArgumentException(e);
        }
      }

      //
      WindowState windowState = update.getProperties().getValue(JuzuPortlet.WINDOW_STATE);
      if (windowState != null) {
        try {
          super.resp.setWindowState(windowState);
        }
        catch (WindowStateException e) {
          throw new IllegalArgumentException(e);
        }
      }
    } else if (response instanceof Response.Error) {
      Response.Error error = (Response.Error)response;
      throw new PortletException(error.getCause());
    }
  }
}
