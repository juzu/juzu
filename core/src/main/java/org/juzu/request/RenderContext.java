package org.juzu.request;

import org.juzu.text.Printer;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class RenderContext extends RequestContext
{

   /** . */
   private final Printer printer;

   public RenderContext(Map<String, String[]> parameters, Printer printer)
   {
      super(parameters);
      this.printer = printer;
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
