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

package juzu.impl.bridge.spi.portlet;

import juzu.EventQueue;
import juzu.Response;
import juzu.bridge.portlet.JuzuPortlet;
import juzu.impl.plugin.application.Application;
import juzu.impl.request.ContextualArgument;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.Method;
import juzu.impl.request.Parameter;

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

  protected PortletInteractionBridge(Application application, Rq req, Rs resp, PortletConfig config, boolean prod) {
    super(application, req, resp, config, prod);

    //
    init();
  }

  protected PortletInteractionBridge(Application application, Rq req, Rs resp, PortletConfig config, Method<?> target, Map<String, String[]> parameters, boolean prod) {
    super(application, req, resp, config, target, parameters, prod);

    //
    init();
  }

  /**
   * Set event producer as a contextual argument.
   */
  private void init() {
    for (Parameter parameter : target.getParameters()) {
      if (parameter instanceof ContextualParameter) {
        final ContextualParameter contextualParameter = (ContextualParameter)parameter;
        if (EventQueue.class.isAssignableFrom(contextualParameter.getType())) {
          final PortletEventProducer producer = new PortletEventProducer();
          arguments.put(contextualParameter.getName(), new ContextualArgument(contextualParameter, producer));
        }
      }
    }
  }

  @Override
  public void send() throws IOException, PortletException {
    if (response instanceof Response.View) {
      Response.View update = (Response.View)response;

      // Parameters
      for (Map.Entry<String, String[]> entry : update.getParameters().entrySet()) {
        super.resp.setRenderParameter(entry.getKey(), entry.getValue());
      }

      //
      Method method = application.getDescriptor().getControllers().getMethodByHandle(update.getTarget());

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
