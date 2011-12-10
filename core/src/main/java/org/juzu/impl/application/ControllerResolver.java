/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.juzu.impl.application;

import org.juzu.AmbiguousResolutionException;
import org.juzu.request.Phase;
import org.juzu.metadata.ApplicationDescriptor;
import org.juzu.metadata.ControllerMethod;

import java.util.ArrayList;
import java.util.List;

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

   private class Foo implements Comparable<Foo>
   {
      int score;
      ControllerMethod m;
      private Foo(ControllerMethod m)
      {
         this.score = m.getArgumentParameters().size() + (m.getType() == desc.getDefaultController() ? 0 : 1000);
         this.m = m;
      }
      public int compareTo(Foo o)
      {
         return score - o.score;
      }
   }

   public ControllerMethod resolve(Phase phase, String methodId) throws AmbiguousResolutionException
   {
      ControllerMethod found = null;

      //
      if (methodId != null)
      {
         for (ControllerMethod method : methods)
         {
            if (method.getId().equals(methodId))
            {
               found = method;
               break;
            }
         }
      }
      else if (phase == Phase.RENDER)
      {
         List<Foo> matches = new ArrayList<Foo>();

         // Collect all index matches
         for (ControllerMethod method : methods)
         {
            if (method.getPhase() == Phase.RENDER && method.getName().equals("index"))
            {
               matches.add(new Foo(method));
            }
         }

         //
         int size = matches.size();
         if (size > 0)
         {
            if (size > 1)
            {
               Foo first = matches.get(0);
               Foo second = matches.get(1);
               if (first.score == second.score)
               {
                  // WE SHOULD NOT HAVE METHOD THAT RESOLVES TO AN AMBIGUITY AND WE CAN ENFORCE
                  // THAT AT COMPILE TIME.
                  // FOR INSTANCE WE COULD USE THE METHOD_ID AS WAY TO FIND THE METHOD
                  // AND ENSURE THAT THE INDEX IS UNIQUE FOR A SINGLER APPLICATION
                  // HAVING THE METHOD NAME AS METHOD ID WOULD HELP HOWEVER WE COULD HAVE TWO
                  // CONTROLLER HAVING SAME IDS
                  throw new AmbiguousResolutionException("Two methods satisfies the index criteria: " +
                     first.m + " and " + second.m);
               }
            }
            found = matches.get(0).m;
         }
      }

      //
      return found;
   }

   private int getScore(ControllerMethod m)
   {
      return m.getArgumentParameters().size() + (m.getType() == desc.getDefaultController() ? 0 : 1000);
   }

/*
         if (m1.getType() != desc.getDefaultController())
         {
            if (m2.getType() == desc.getDefaultController())
            {
               return -1;
            }
         }
         else
         {
            if (m2.getType() != desc.getDefaultController())
            {
               return 1;
            }
         }

         //
         int s1 = m1.getArgumentParameters().size();
         int s2 = m1.getArgumentParameters().size();
         if (s1 == s2)
         {
            return 0;
         }
         else if (s1 < s2)
         {
            return 1;
         }
         else
         {
            return s2;
         }
*/
}
