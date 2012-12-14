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

package juzu.test.protocol.mock;

import juzu.Response;
import juzu.impl.plugin.application.ApplicationContext;
import juzu.impl.bridge.spi.ActionBridge;
import juzu.impl.common.MethodHandle;
import juzu.request.ClientContext;
import juzu.impl.bridge.spi.DispatchSPI;
import juzu.request.Phase;
import juzu.test.AbstractTestCase;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockActionBridge extends MockRequestBridge implements ActionBridge {

  /** . */
  private Response response;

  public MockActionBridge(ApplicationContext application, MockClient client, MethodHandle target, Map<String, String[]> parameters) {
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
    assertRender(application.getDescriptor().getControllers().getMethodById(expectedTarget).getHandle(), expectedArguments);
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

  public void setResponse(Response response) throws IllegalStateException, IOException {
    if (response instanceof Response.View || response instanceof Response.Redirect) {
      this.response = response;
    } else {
      throw new IllegalArgumentException();
    }
  }
}
