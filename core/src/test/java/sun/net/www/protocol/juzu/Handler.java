package sun.net.www.protocol.juzu;

import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.ram.RAMURLStreamHandler;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Handler extends RAMURLStreamHandler
{

   /** . */
   public static RAMFileSystem FS;
   
   static
   {
      try
      {
         FS = new RAMFileSystem();
      }
      catch (IOException e)
      {
         throw new Error(e);
      }
   }

   public Handler()
   {
      super(FS);
   }
}
