package template.url.resolution;

import org.juzu.Render;
import org.juzu.Path;
import org.juzu.template.Template;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A
{

   @Path("index.gtmpl")
   private Template index;

   @Render
   public void foo() { }

   @Render
   public void bar(String s) { }

}
