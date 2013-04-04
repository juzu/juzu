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

package juzu.test.protocol.mock;

import juzu.Response;
import juzu.impl.bridge.spi.ActionBridge;
import juzu.impl.common.MethodHandle;
import juzu.impl.plugin.application.ApplicationLifeCycle;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.request.ClientContext;
import juzu.impl.bridge.spi.DispatchSPI;
import juzu.request.Phase;
import juzu.test.AbstractTestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockActionBridge extends MockRequestBridge implements ActionBridge {

  public MockActionBridge(ApplicationLifeCycle<?, ?> application, MockClient client, MethodHandle target, Map<String, String[]> parameters) {
    super(application, client, target, parameters);
  }

  public ClientContext getClientContext() {
    throw new UnsupportedOperationException();
  }

  public String assertUpdate() {
    if (response instanceof Response.View) {
      Response.View update = (Response.View)response;
      DispatchSPI spi = createDispatch(Phase.VIEW, update.getTarget(), update.getParameters());
      Phase.View.Dispatch dispatch = new Phase.View.Dispatch(spi);
      return dispatch.with(update.getProperties()).toString();
    }
    else {
      throw AbstractTestCase.failure("Was expecting an update instead of " + response);
    }
  }

  public void assertNoResponse() {
    assertResponse(null);
  }

  public void assertRedirect(String location) {
    assertResponse(new Response.Redirect(location));
  }

  public void assertRender(final MethodHandle expectedTarget, Map<String, String> expectedArguments) {
    final HashMap<String, String[]> a = new HashMap<String, String[]>();
    for (Map.Entry<String, String> entry : expectedArguments.entrySet()) {
      a.put(entry.getKey(), new String[]{entry.getValue()});
    }
    Response.View resp = new Response.View() {
      @Override
      public MethodHandle getTarget() {
        return expectedTarget;
      }
      @Override
      public Map<String, String[]> getParameters() {
        return a;
      }
    };
    assertResponse(resp);
  }

  public void assertRender(String expectedTarget, Map<String, String> expectedArguments) {
    assertRender(application.getPlugin(ControllerPlugin.class).getDescriptor().getMethodById(expectedTarget).getHandle(), expectedArguments);
  }

  private void assertResponse(Response expectedResponse) {
    if (expectedResponse instanceof Response.View) {
      Response.View expected = (Response.View)expectedResponse;
      Response.View resp = (Response.View)response;

      AbstractTestCase.assertEquals(expected.getParameters().size(), expected.getParameters().size());
      for (String key : resp.getParameters().keySet()) {
        AbstractTestCase.assertEquals(
          Arrays.asList(expected.getParameters().get(key)),
          Arrays.asList(resp.getParameters().get(key)));
      }
    }
    else {
      AbstractTestCase.assertEquals("Was expecting a response " + expectedResponse + " instead of  " + response,
        expectedResponse,
        response);
    }
  }
}
