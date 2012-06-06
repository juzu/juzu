package juzu.plugin.less.impl.lesser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class URLLessContext implements LessContext
{

   /** . */
   private final URL baseURL;

   public URLLessContext(URL baseURL)
   {
      this.baseURL = baseURL;
   }

   public String load(String ref)
   {
      try
      {
         URI uri = baseURL.toURI().resolve(ref);
         URL url = uri.toURL();
         InputStream in = url.openStream();
         ByteArrayOutputStream baos = Lesser.append(in, new ByteArrayOutputStream());
         return baos.toString();
      }
      catch (Exception e)
      {
         return null;
      }
   }
}
