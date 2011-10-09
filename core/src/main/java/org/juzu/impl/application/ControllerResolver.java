package org.juzu.impl.application;

import org.juzu.AmbiguousResolutionException;
import org.juzu.application.ApplicationDescriptor;
import org.juzu.application.Phase;
import org.juzu.impl.request.ControllerMethod;
import org.juzu.impl.request.ControllerParameter;
import org.juzu.impl.utils.Safe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Resolves a controller for a given input.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ControllerResolver
{

   /** . */
   private final List<ControllerMethod> methods;

   public ControllerResolver(ApplicationDescriptor desc) throws NullPointerException
   {
      if (desc == null)
      {
         throw new NullPointerException("No null application descriptor accepted");
      }

      //
      this.methods = desc.getControllerMethods();
   }

   public ControllerResolver(ControllerMethod... methods)
   {
      this.methods = Safe.unmodifiableList(methods);
   }

   // Longuest path ?
   // Order

   // foo=bar juu=daa
   // foo=bar

   private static class Match
   {

      /** . */
      private final ControllerMethod method;

      /** . */
      private final int score;

      private Match(ControllerMethod method, int score)
      {
         this.method = method;
         this.score = score;
      }
   }

   /**
    * Initial implementation:
    * - simple routing based on phase and best score
    * - not optimized
    *
    * @param phase the expected phase
    * @param parameters the parameters
    * @return the render descriptor or null if nothing could be resolved
    */
   public ControllerMethod resolve(Phase phase, Map<String, String[]> parameters) throws AmbiguousResolutionException
   {
      TreeMap<Integer, List<Match>> matches = new TreeMap<Integer, List<Match>>();
      out:
      for (ControllerMethod method : methods)
      {
         if (method.getPhase() == phase)
         {
            int score = 0;
            List<List<ControllerParameter>> listList = new ArrayList<List<ControllerParameter>>(2);
            listList.add(method.getAnnotationParameters());
            listList.add(method.getArgumentParameters());
            for (List<ControllerParameter> list : listList)
            {
               for (ControllerParameter cp : list)
               {
                  String[] val = parameters.get(cp.getName());
                  if (val == null || val.length == 0 || (cp.getValue() != null && !cp.getValue().equals(val[0])))
                  {
                     continue out;
                  }
                  score += cp.getValue() == null ? 1 : 2;
               }
            }
            List<Match> scoreMatches = matches.get(score);
            if (scoreMatches == null)
            {
               matches.put(score, scoreMatches = new ArrayList<Match>());
            }
            scoreMatches.add(new Match(method, score));
         }
      }

      // Returns the best match
      Map.Entry<Integer, List<Match>> a = matches.lastEntry();
      if (a != null)
      {
         List<Match> b = a.getValue();
         if (b.size() > 1)
         {
            throw new AmbiguousResolutionException("Could not resolve resolution");
         }
         return b.get(0).method;
      }

      //
      return null;
   }
}
