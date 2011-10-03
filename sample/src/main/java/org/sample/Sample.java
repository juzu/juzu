package org.sample;

import org.juzu.Action;
import org.juzu.Render;
import org.juzu.Resource;
import org.juzu.application.ApplicationDescriptor;
import org.juzu.application.RenderLiteral;
import org.juzu.template.Template;
import org.juzu.text.Printer;

import javax.inject.Inject;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Sample
{

   static
   {
      // Generated
      ApplicationDescriptor desc = SampleApplication.DESCRIPTOR;
   }

   @Inject
   @Resource("MyTemplate.gtmpl")
   private Template template;

   @Inject
   private Printer printer;

   @Action
   public RenderLiteral action()
   {
      // Render literal
      return Sample_.render;
   }

   @Render
   public void render() throws IOException
   {
      // A generated template literal for MyTemplate
      org.sample.templates.MyTemplate literal;

      // Render template
      template.render(printer);
   }
}
