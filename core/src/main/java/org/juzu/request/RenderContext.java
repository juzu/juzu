package org.juzu.request;

import org.juzu.URLBuilder;
import org.juzu.application.ControllerMethod;
import org.juzu.application.Phase;
import org.juzu.text.Printer;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class RenderContext extends RequestContext
{

   /** . */
   private final Printer printer;

   /** . */
   private URLBuilderContext urlBuilderContext;

   public RenderContext(Map<String, String[]> parameters, Printer printer, URLBuilderContext urlBuilderContext)
   {
      super(parameters);

      //
      this.printer = printer;
      this.urlBuilderContext = urlBuilderContext;
   }

   @Override
   public Phase getPhase()
   {
      return Phase.RENDER;
   }

   public URLBuilder createURLBuilder(ControllerMethod method)
   {
      return urlBuilderContext.createURLBuilder(method.getPhase());
   }

   /**
    * Returns the current printer.
    *
    * @return the printer
    */
   public Printer getPrinter()
   {
      return printer;
   }
}
