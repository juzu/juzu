package org.juzu.test.request;

import org.juzu.impl.request.RenderBridge;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockRenderBridge extends MockMimeBridge implements RenderBridge
{
   public MockRenderBridge(MockClient client)
   {
      super(client);
   }
}
