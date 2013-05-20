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
  public Response.Content index() {
    return Response.ok("<a id='trigger' href='" + A_.action() + "'>click</a>");
  }
}
