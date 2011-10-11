package template_url;

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

   @Render
   public void bar(String s) { }

}
