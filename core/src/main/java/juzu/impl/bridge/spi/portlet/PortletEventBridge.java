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

import juzu.Event;
import juzu.impl.bridge.spi.EventBridge;
import juzu.impl.common.Introspector;
import juzu.impl.plugin.application.ApplicationContext;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.Method;
import juzu.impl.request.Parameter;
import juzu.request.Phase;

import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletEventBridge extends PortletRequestBridge<EventRequest, EventResponse> implements EventBridge {

  public PortletEventBridge(
      ApplicationContext application,
      EventRequest req,
      EventResponse resp,
      Method<?> target,
      Map<String, String[]> parameters,
      boolean prod) {
    super(application, req, resp, target, parameters, prod);

    // Care of event contextual arguments
    for (Parameter parameter : target.getParameters()) {
      if (parameter instanceof ContextualParameter) {
        ContextualParameter contextualParameter = (ContextualParameter)parameter;
        if (Event.class.isAssignableFrom(contextualParameter.getType())) {
          Class payloadType = Introspector.resolveToClass(contextualParameter.getGenericType(), Event.class, 0);
          if (payloadType.isInstance(req.getEvent().getValue())) {
            Event event = new Event(req.getEvent().getName(), req.getEvent().getValue());
            arguments.put(parameter.getName(), contextualParameter.create(event));
          }
        }
      }
    }
  }

  @Override
  protected Phase getPhase() {
    return Phase.EVENT;
  }
}
