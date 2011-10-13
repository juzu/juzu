package template.url.invalid_method_args;

import org.juzu.Render;
import org.juzu.Resource;
import org.juzu.template.Template;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A
{

   @Resource("index.gtmpl")
   private Template index;

   @Render
   public void foo() { }

}
