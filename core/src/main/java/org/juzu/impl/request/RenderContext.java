package org.juzu.impl.request;

import org.juzu.URLBuilder;
import org.juzu.application.Phase;
import org.juzu.text.Printer;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class RenderContext extends RequestContext<RenderBridge>
{

   public RenderContext(ClassLoader classLoader, RenderBridge bridge)
   {
      super(classLoader, bridge);
   }

   @Override
   public final Phase getPhase()
   {
      return Phase.RENDER;
   }

   public URLBuilder createURLBuilder(ControllerMethod method)
   {
      URLBuilder builder = bridge.createURLBuilder(method.getPhase());

      //
      builder.setParameter("op", method.getId());

      //
      return builder;
   }

   public URLBuilder createURLBuilder(ControllerMethod method, Object arg)
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
   public Printer getPrinter()
   {
      return bridge.getPrinter();
   }

   @Override
   public Map<Object, Object> getContext(Scope scope)
   {
      switch (scope)
      {
         case FLASH:
            return bridge.getFlashContext();
         case RENDER:
         case REQUEST:
            return bridge.getRequestContext();
         case ACTION:
            return null;
         case SESSION:
            return bridge.getSessionContext();
         case IDENTITY:
            return bridge.getIdentityContext();
         default:
            throw new AssertionError();
      }
   }
}
