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

import junit.framework.Assert;
import juzu.Response;
import juzu.impl.bridge.spi.DispatchBridge;
import juzu.impl.common.MethodHandle;
import juzu.impl.runtime.ApplicationRuntime;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.request.Phase;
import juzu.request.ResponseParameter;
import juzu.test.AbstractTestCase;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockActionBridge extends MockRequestBridge {

  public MockActionBridge(ApplicationRuntime<?, ?> application, MockClient client, MethodHandle target, Map<String, String[]> parameters) {
    super(application, client, Phase.ACTION, target, parameters);
  }

  public String assertUpdate() {
    if (response instanceof Response.View) {
      Response.View view = (Response.View)response;
      Phase.View.Dispatch update = (Phase.View.Dispatch)view;
      DispatchBridge spi = createDispatch(Phase.VIEW, update.getTarget(), update.getParameters());
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
    Assert.assertEquals(expectedTarget, ((Phase.View.Dispatch)response).getTarget());
    Map<String, ResponseParameter> parameters = ((Phase.View.Dispatch)response).getParameters();
    Assert.assertEquals(expectedArguments.keySet(), parameters.keySet());
    for (Map.Entry<String, String> argument : expectedArguments.entrySet()) {
      Assert.assertEquals(1, parameters.get(argument.getKey()).size());
      Assert.assertEquals(argument.getValue(), parameters.get(argument.getKey()).get(0));
    }
  }

  public void assertRender(String expectedTarget, Map<String, String> expectedArguments) {
    assertRender(application.resolveBean(ControllerPlugin.class).getDescriptor().getMethodById(expectedTarget).getHandle(), expectedArguments);
  }

  private void assertResponse(Response expectedResponse) {
    if (expectedResponse instanceof Response.View) {
      throw new UnsupportedOperationException("fixme");
    }
    else {
      AbstractTestCase.assertEquals("Was expecting a response " + expectedResponse + " instead of  " + response, expectedResponse, response);
    }
  }
}
