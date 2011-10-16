package org.juzu.impl.template;

import java.io.IOException;
import java.io.Reader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class OffsetCharStream extends SimpleCharStream
{

   /** . */
   public int beginOffset;

   /** . */
   public int currentOffset;

   public OffsetCharStream(OffsetReader r)
   {
      super(r);
   }

   public char BeginToken() throws IOException
   {
      char c = super.BeginToken();
      beginOffset = currentOffset;
      return c;
   }

   public char readChar() throws IOException
   {
      char c = super.readChar();
      currentOffset++;
      return c;
   }

   public void backup(int amount)
   {
      super.backup(amount);
      currentOffset -= amount;
   }

   public CharSequence getData()
   {
      return ((OffsetReader)inputStream).getData();
   }
}
