package org.sample;

import org.juzu.View;
import org.juzu.Path;
import org.juzu.template.Template;
import javax.inject.Inject;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Controller
{

   @Inject
   @Path("index.gtmpl")
   Template index;


   @View
   public void index() throws IOException
   {
      index.render();
   }
}
