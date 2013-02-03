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

package bridge.portlet.event.consume;

import juzu.Action;
import juzu.Consumes;
import juzu.Event;
import juzu.Response;
import juzu.View;
import juzu.impl.bridge.portlet.ConsumeEventTestCase;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  @Action
  public Response.View action() {
    return A_.index();
  }

  @Consumes("event1")
  public Response.View event1(Event<Integer> integerEvent, Event<String> stringEvent) {
    if (integerEvent != null) {
      ConsumeEventTestCase.eventNames.addLast(integerEvent.getName());
      ConsumeEventTestCase.eventPayloads.addLast(integerEvent.getPayload());
      ConsumeEventTestCase.sourceNames.add("event1");
    }
    if (stringEvent != null) {
      ConsumeEventTestCase.eventNames.addLast(stringEvent.getName());
      ConsumeEventTestCase.eventPayloads.addLast(stringEvent.getPayload());
      ConsumeEventTestCase.sourceNames.add("event1");
    }
    return A_.index();
  }

  @Consumes("event2")
  public Response.View event2(Event<Integer> integerEvent, Event<String> stringEvent) {
    if (integerEvent != null) {
      ConsumeEventTestCase.eventNames.addLast(integerEvent.getName());
      ConsumeEventTestCase.eventPayloads.addLast(integerEvent.getPayload());
      ConsumeEventTestCase.sourceNames.add("event2");
    }
    if (stringEvent != null) {
      ConsumeEventTestCase.eventNames.addLast(stringEvent.getName());
      ConsumeEventTestCase.eventPayloads.addLast(stringEvent.getPayload());
      ConsumeEventTestCase.sourceNames.add("event2");
    }
    return A_.index();
  }

  @Consumes()
  public Response.View event3(Event<Integer> integerEvent, Event<String> stringEvent) {
    if (integerEvent != null) {
      ConsumeEventTestCase.eventNames.addLast(integerEvent.getName());
      ConsumeEventTestCase.eventPayloads.addLast(integerEvent.getPayload());
      ConsumeEventTestCase.sourceNames.add("event3");
    }
    if (stringEvent != null) {
      ConsumeEventTestCase.eventNames.addLast(stringEvent.getName());
      ConsumeEventTestCase.eventPayloads.addLast(stringEvent.getPayload());
      ConsumeEventTestCase.sourceNames.add("event3");
    }
    return A_.index();
  }

  @View
  public Response.Render index() {
    return Response.ok("<a id='trigger' href='" + A_.action() + "'>click</a>");
  }
}
