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

import juzu.Event;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.spi.EventBridge;
import juzu.impl.common.Introspector;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.ControlParameter;
import juzu.impl.request.Method;
import juzu.request.Phase;

import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletConfig;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletEventBridge extends PortletInteractionBridge<EventRequest, EventResponse> implements EventBridge {

  public PortletEventBridge(
      Bridge bridge,
      EventRequest req,
      EventResponse resp,
      PortletConfig config,
      Method<?> target,
      Map<String, String[]> parameters) {
    super(bridge, req, resp, config, target, parameters);

    // Set event as part of contextual arguments
    for (ControlParameter parameter : target.getParameters()) {
      if (parameter instanceof ContextualParameter) {
        ContextualParameter contextualParameter = (ContextualParameter)parameter;
        if (Event.class.isAssignableFrom(contextualParameter.getType())) {
          Class payloadType = Introspector.resolveToClass(contextualParameter.getGenericType(), Event.class, 0);
          if (payloadType.isInstance(req.getEvent().getValue())) {
            Event event = new Event(req.getEvent().getName(), req.getEvent().getValue());
            arguments.put(parameter, event);
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
