package org.juzu.test.request;

import org.juzu.text.WriterPrinter;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockPrinter extends WriterPrinter
{

   public MockPrinter()
   {
      super(new StringBuilder());
   }

   public StringBuilder getContent()
   {
      return (StringBuilder)writer;
   }
}
