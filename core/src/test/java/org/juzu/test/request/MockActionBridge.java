package org.juzu.test.request;

import org.juzu.Response;
import org.juzu.impl.request.ActionBridge;

import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockActionBridge extends MockRequestBridge implements ActionBridge
{

   /** . */
   private final List<MockResponse> responses = new ArrayList<MockResponse>();

   public MockActionBridge(MockClient client)
   {
      super(client);
   }

   public Response createResponse()
   {
      MockResponse response = new MockResponse();
      responses.add(response);
      return response;
   }
}
