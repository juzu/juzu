package org.juzu.template;

import org.juzu.impl.spi.template.TemplateStub;
import org.juzu.text.Printer;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateRenderContext
{

   /** . */
   private final Printer printer;

   /** . */
   private final Map<String, ?> attributes;

   /** . */
   private final Locale locale;

   public TemplateRenderContext(Printer printer)
   {
      this(printer, Collections.<String, Object>emptyMap());
   }

   public TemplateRenderContext(Printer printer, Map<String, ?> attributes)
   {
      this(printer, attributes, null);
   }

   public TemplateRenderContext(Printer printer, Locale locale)
   {
      this(printer, Collections.<String, Object>emptyMap(), locale);
   }

   public TemplateRenderContext(Printer printer, Map<String, ?> attributes, Locale locale)
   {
      this.printer = printer;
      this.locale = locale;
      this.attributes = attributes;
   }

   public Map<String, ?> getAttributes()
   {
      return attributes;
   }

   public Locale getLocale()
   {
      return locale;
   }

   public Printer getPrinter()
   {
      return printer;
   }

   public TemplateStub resolveTemplate(String path)
   {
      return null;
   }

   public Object resolveBean(String expression)
   {
      return null;
   }
}
