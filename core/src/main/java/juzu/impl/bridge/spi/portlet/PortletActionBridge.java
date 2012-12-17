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
import juzu.impl.plugin.application.ApplicationContext;
import juzu.impl.bridge.spi.ActionBridge;
import juzu.impl.request.ContextualArgument;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.Method;
import juzu.bridge.portlet.JuzuPortlet;
import juzu.impl.request.Parameter;
import juzu.request.ClientContext;
import juzu.request.Phase;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletActionBridge extends PortletRequestBridge<ActionRequest, ActionResponse> implements ActionBridge {

  /** . */
  private final PortletClientContext clientContext;

  public PortletActionBridge(ApplicationContext application, ActionRequest request, ActionResponse response, boolean prod) {
    super(application, request, response, prod);

    // Set event producer as contextual argument
    for (Parameter parameter : target.getParameters()) {
      if (parameter instanceof ContextualParameter) {
        final ContextualParameter contextualParameter = (ContextualParameter)parameter;
        if (EventQueue.class.isAssignableFrom(contextualParameter.getType())) {
          final PortletEventProducer producer = new PortletEventProducer();
          arguments.put(contextualParameter.getName(), new ContextualArgument(contextualParameter, producer));
        }
      }
    }


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

  public void setResponse(Response response) throws IllegalStateException, IOException {
    super.setResponse(response);
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
    }
    else if (response instanceof Response.Redirect) {
      Response.Redirect redirect = (Response.Redirect)response;
      super.resp.sendRedirect(redirect.getLocation());
    } else {
      throw new IllegalArgumentException();
    }
  }
}
