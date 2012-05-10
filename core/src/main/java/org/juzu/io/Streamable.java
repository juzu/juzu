package org.juzu.io;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface Streamable<S extends Stream>
{

   public static class CharSequence implements Streamable<Stream.Char>
   {

      private final java.lang.CharSequence s;

      public CharSequence(java.lang.CharSequence s)
      {
         this.s = s;
      }

      public void send(Stream.Char stream) throws IOException
      {
         stream.append(s);
      }
   }

   public static class InputStream implements Streamable<Stream.Binary>
   {

      /** . */
      private final java.io.InputStream in;

      public InputStream(java.io.InputStream in)
      {
         this.in = in;
      }

      public void send(Stream.Binary stream) throws IOException
      {
         byte[] buffer = new byte[256];
         for (int l;(l = in.read(buffer)) != -1;)
         {
            stream.append(buffer, 0, l);
         }
      }
   }

   void send(S stream) throws IOException;

}
