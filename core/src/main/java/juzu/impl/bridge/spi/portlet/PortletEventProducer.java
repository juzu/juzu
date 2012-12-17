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
import juzu.impl.bridge.EventBridge;
import juzu.impl.bridge.spi.ActionBridge;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.request.Request;

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
      if (bridge instanceof ActionBridge) {
        PortletActionBridge actionBridge = (PortletActionBridge)bridge;
        actionBridge.resp.setEvent(name, serializablePayload);
      } else if (bridge instanceof EventBridge) {
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
