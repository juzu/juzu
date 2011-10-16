package org.juzu.test.request;

import org.juzu.URLBuilder;
import org.juzu.application.Phase;
import org.juzu.impl.request.MimeBridge;
import org.juzu.text.Printer;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockMimeBridge extends MockRequestBridge implements MimeBridge
{

   /** . */
   private final MockPrinter printer;

   public MockMimeBridge(MockClient client)
   {
      super(client);

      //
      printer = new MockPrinter();
   }

   public URLBuilder createURLBuilder(Phase phase)
   {
      return new MockURLBuilder(phase);
   }

   public Printer getPrinter()
   {
      return printer;
   }
}
