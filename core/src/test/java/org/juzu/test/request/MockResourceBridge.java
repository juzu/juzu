package org.juzu.test.request;

import org.juzu.impl.request.ResourceBridge;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockResourceBridge extends MockMimeBridge implements ResourceBridge
{
   public MockResourceBridge(MockClient client)
   {
      super(client);
   }
}
