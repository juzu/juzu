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
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.request.Request;
import juzu.request.Phase;

import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletEventProducer implements EventQueue {

  public void send(String name) {
    send(name, null);
  }

  public void send(String name, Object payload) {
    Request request = Request.getCurrent();
    RequestBridge bridge = request.getBridge();
    if (payload == null || payload instanceof Serializable) {
      Serializable serializablePayload = (Serializable)payload;
      if (bridge.getPhase() == Phase.ACTION) {
        PortletActionBridge actionBridge = (PortletActionBridge)bridge;
        actionBridge.resp.setEvent(name, serializablePayload);
      } else if (bridge.getPhase() == Phase.EVENT) {
        PortletEventBridge actionBridge = (PortletEventBridge)bridge;
        actionBridge.resp.setEvent(name, serializablePayload);
      } else {
        throw new IllegalStateException("Cannot send event");
      }
    } else {
      throw new IllegalArgumentException("Payload must be serializable for a portlet event");
    }
  }
}
