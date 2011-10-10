package org.juzu.impl.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class IO
{

   public static String read(InputStream in) throws IOException
   {
      return read(in, "UTF-8");
   }

   public static String read(InputStream in, String charsetName) throws IOException
   {
      byte[] buffer = new byte[256];
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      for (int l;(l = in.read(buffer)) != -1;)
      {
         baos.write(buffer, 0, l);
      }
      return baos.toString();
   }

}
