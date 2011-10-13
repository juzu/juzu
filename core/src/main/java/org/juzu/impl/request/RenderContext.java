package org.juzu.impl.request;

import org.juzu.URLBuilder;
import org.juzu.application.Phase;
import org.juzu.text.Printer;

import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class RenderContext extends RequestContext
{

   public RenderContext(ClassLoader classLoader)
   {
      super(classLoader);
   }

   @Override
   public final Phase getPhase()
   {
      return Phase.RENDER;
   }

   protected abstract URLBuilder createURLBuilder(Phase phase);

   public final URLBuilder createURLBuilder(ControllerMethod method)
   {
      URLBuilder builder = createURLBuilder(method.getPhase());

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

   public final URLBuilder createURLBuilder(ControllerMethod method, Object arg)
   {
      URLBuilder builder = createURLBuilder(method);

      //
      ControllerParameter param = method.getArgumentParameters().get(0);
      if (arg != null)
      {
         builder.setParameter(param.getName(), String.valueOf(arg));
      }

      //
      return builder;
   }

   public URLBuilder createURLBuilder(ControllerMethod method, Object[] args)
   {
      URLBuilder builder = createURLBuilder(method);

      // Fill in argument parameters
      for (int i = 0;i < args.length;i++)
      {
         Object value = args[i];
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
   public abstract Printer getPrinter();
}
