package org.sample.controllers;

import org.juzu.Action;
import org.juzu.Binding;
import org.juzu.Render;
import org.juzu.Response;
import org.juzu.Resource;
import org.juzu.application.ApplicationDescriptor;
import org.juzu.template.Template;
import org.juzu.text.Printer;

import javax.enterprise.context.SessionScoped;
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
      ApplicationDescriptor desc = org.sample.SampleApplication.DESCRIPTOR;
   }

   @Inject
   @Resource("MyTemplate.gtmpl")
   private Template template;

   @Inject
   private Printer printer;

   @Inject
   @SessionScoped
   private Counter counter;

   @Action
   public void action()
   {
      // Render phase
      org.sample.SampleApplication.foo("bar");
   }

   @Render
   public void render() throws IOException
   {
      // A generated template literal for MyTemplate
      org.sample.templates.MyTemplate literal;

      // Render template
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("counter", counter.getValue());
      template.render(printer, data);
   }

   @Render
   public void foo(String name) throws IOException
   {
      System.out.println("foo : " + name);
      Map<String, Object> data = new HashMap<String, Object>();
      template.render(printer, data);
   }

   @Action(parameters = @Binding(name = "op", value = "increment"))
   public void increment() throws IOException
   {
      counter.increment();
      org.sample.SampleApplication.render();
   }
}
