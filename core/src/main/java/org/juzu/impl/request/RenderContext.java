package org.juzu.impl.request;

import org.juzu.URLBuilder;
import org.juzu.application.Phase;
import org.juzu.text.Printer;

import java.util.List;
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
      URLBuilder builder = urlBuilderContext.createURLBuilder(method.getPhase());

      // Fill in bound parameters
      List<ControllerParameter> parameters = method.getAnnotationParameters();
      int size = parameters.size();
      for (int i = 0;i < size;i++)
      {
         ControllerParameter parameter = parameters.get(i);
         builder.setParameter(parameter.getName(), parameter.getValue());
      }

      //
      return builder;
   }

   public URLBuilder createURLBuilder(ControllerMethod method, Object value)
   {
      URLBuilder builder = createURLBuilder(method);

      //
      ControllerParameter param = method.getArgumentParameters().get(0);
      if (value != null)
      {
         builder.setParameter(param.getName(), String.valueOf(value));
      }

      //
      return builder;
   }

   public URLBuilder createURLBuilder(ControllerMethod method, Object[] values)
   {
      URLBuilder builder = createURLBuilder(method);

      // Fill in argument parameters
      for (int i = 0;i < values.length;i++)
      {
         Object value = values[i];
         if (value != null)
         {
            builder.setParameter(method.getArgumentParameters().get(i).getName(), String.valueOf(value));
         }
      }
      return builder;
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
