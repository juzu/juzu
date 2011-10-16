package org.juzu.impl.template;

import java.io.IOException;
import java.io.Reader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class OffsetReader extends Reader
{

   /** . */
   private final Reader in;

   /** . */
   private final StringBuilder data;

   public OffsetReader(Reader in)
   {
      this.in = in;
      this.data = new StringBuilder();
   }

   public StringBuilder getData()
   {
      return data;
   }

   @Override
   public int read(char[] cbuf, int off, int len) throws IOException
   {
      int read = in.read(cbuf, off, len);
      if (read > 0)
      {
         data.append(cbuf, off, read);
      }
      return read;
   }

   @Override
   public void close() throws IOException
   {
   }
}
