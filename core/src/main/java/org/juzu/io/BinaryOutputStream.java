package org.juzu.io;

import java.io.IOException;
import java.io.OutputStream;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BinaryOutputStream implements Stream.Binary
{

   /** . */
   private final OutputStream delegate;

   public BinaryOutputStream(OutputStream delegate)
   {
      this.delegate = delegate;
   }

   public Stream.Binary append(byte[] data) throws IOException
   {
      delegate.write(data);
      return this;
   }

   public Stream.Binary append(byte[] data, int off, int len) throws IOException
   {
      delegate.write(data, off, len);
      return this;
   }

   public void close() throws IOException
   {
      delegate.close();
   }

   public void flush() throws IOException
   {
      delegate.flush();
   }
}
