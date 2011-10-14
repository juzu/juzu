package org.sample.controllers;

import org.juzu.Action;
import org.juzu.Render;
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
      Sample_.foo("bar");
   }

   @Render
   public void index() throws IOException
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

   @Action
   public void increment() throws IOException
   {
      counter.increment();
      Sample_.index();
   }
}
