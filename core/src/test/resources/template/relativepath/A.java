package template.relativepath;

import org.juzu.Path;
import org.juzu.Render;
import org.juzu.template.Template;
import org.juzu.text.Printer;

import javax.inject.Inject;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A
{

   @Path("folder/index.gtmpl")
   @Inject
   Template index;

   @Inject
   Printer printer;

   @Render
   public void index() throws IOException
   {
      index.render(printer);
   }
}
