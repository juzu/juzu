package org.juzu.impl.application;

import org.juzu.impl.spi.template.TemplateStub;
import org.juzu.template.TemplateRenderContext;
import org.juzu.text.Printer;

import java.util.Locale;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationTemplateRenderContext extends TemplateRenderContext
{

   /** . */
   private final ApplicationContext applicationContext;

   public ApplicationTemplateRenderContext(ApplicationContext applicationContext, Printer printer, Map<String, ?> attributes, Locale locale)
   {
      super(printer, attributes, locale);

      //
      this.applicationContext = applicationContext;
   }

   @Override
   public TemplateStub resolveTemplate(String path)
   {
      return applicationContext.resolveTemplateStub(path);
   }

   @Override
   public Object resolveBean(String name)
   {
      return applicationContext.resolveBean(name);
   }
}
