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

import juzu.test.protocol.portlet.JuzuPortlet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TestPortlet extends JuzuPortlet {

  @Override
  public void processAction(ActionRequest req, ActionResponse resp) throws PortletException, IOException {
    super.processAction(req, resp);

    // Send an event
    resp.setEvent("event1", 3);
    resp.setEvent("event1", "4");
    resp.setEvent("event2", 5);
    resp.setEvent("event3", "6");
  }
}
