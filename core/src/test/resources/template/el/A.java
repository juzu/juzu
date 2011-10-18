package template.el;

import org.juzu.Path;
import org.juzu.Render;
import org.juzu.template.Template;
import org.juzu.text.Printer;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Named("a")
@RequestScoped
public class A
{

   @Inject
   @Path("index.gtmpl")
   Template index;

   @Inject
   Printer printer;

   String value;

   public String getValue()
   {
      return value;
   }

   @Render
   public void index() throws IOException
   {
      value = "A";
      index.render(printer);
   }
}
