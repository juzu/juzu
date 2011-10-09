package org.sample;

import org.juzu.Action;
import org.juzu.Render;
import org.juzu.Resource;
import org.juzu.application.ApplicationDescriptor;
import org.juzu.application.PhaseLiteral;
import org.juzu.template.Template;
import org.juzu.text.Printer;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
   public PhaseLiteral action()
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
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("action", "" + Sample_.actionURL());
      data.put("render", "" + Sample_.renderURL());
      data.put("foo", "" + Sample_.fooURL("bar"));
      template.render(printer, data);
   }

   @Render
   public void foo(String name) throws IOException
   {
      System.out.println("foo");
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("action", "" + Sample_.actionURL());
      data.put("render", "" + Sample_.renderURL());
      data.put("foo", "" + Sample_.fooURL("bar"));
      template.render(printer, data);
   }
}
