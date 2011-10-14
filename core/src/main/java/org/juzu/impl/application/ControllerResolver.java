package org.juzu.impl.application;

import org.juzu.AmbiguousResolutionException;
import org.juzu.application.ApplicationDescriptor;
import org.juzu.application.Phase;
import org.juzu.impl.request.ControllerMethod;

import java.util.List;
import java.util.Map;

/**
 * Resolves a controller for a given input.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ControllerResolver
{

   /** . */
   private final List<ControllerMethod> methods;

   /** . */
   private final ApplicationDescriptor desc;

   public ControllerResolver(ApplicationDescriptor desc) throws NullPointerException
   {
      if (desc == null)
      {
         throw new NullPointerException("No null application descriptor accepted");
      }

      //
      this.desc = desc;
      this.methods = desc.getControllerMethods();
   }

   /**
    * Initial implementation:
    * - simple routing based on phase and best score
    * - not optimized
    *
    * @param phase the expected phase
    * @param parameters the parameters
    * @return the render descriptor or null if nothing could be resolved
    * @throws AmbiguousResolutionException when more than one controller method is resolved
    */
   public ControllerMethod resolve(Phase phase, Map<String, String[]> parameters) throws AmbiguousResolutionException
   {
      ControllerMethod found = null;

      //
      String[] op = parameters.get("op");
      if (op != null && op.length > 0)
      {
         for (ControllerMethod method : methods)
         {
            if (method.getId().equals(op[0]))
            {
               found = method;
               break;
            }
         }
      }
      else if (phase == Phase.RENDER)
      {
         for (ControllerMethod method : methods)
         {
            if (
               method.getPhase() == Phase.RENDER &&
               method.getName().equals("index") &&
               method.getArgumentParameters().isEmpty())
            {
               if (desc.getDefaultController() == method.getType())
               {
                  return method;
               }
               else if (found == null)
               {
                  found = method;
               }
               else
               {
                  throw new AmbiguousResolutionException();
               }
            }
         }
      }

      //
      return found;
   }
}
