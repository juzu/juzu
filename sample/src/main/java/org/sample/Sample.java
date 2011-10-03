package org.sample;

import org.juzu.template.Template;
import org.juzu.template.TemplateRenderer;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Sample
{

   @Template("MyTemplate.gtmpl")
   private TemplateRenderer templateRenderer;

   public void foo()
   {
      // A generated template literal for MyTemplate
      MyTemplate literal;
   }

}
