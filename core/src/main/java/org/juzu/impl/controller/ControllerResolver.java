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

package org.juzu.impl.controller;

import org.juzu.AmbiguousResolutionException;
import org.juzu.impl.controller.descriptor.ControllerDescriptor;
import org.juzu.impl.controller.descriptor.ControllerMethod;
import org.juzu.request.Phase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves controller method algorithm.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ControllerResolver
{

   /** . */
   private final ControllerMethod[] methods;

   /** . */
   private final ControllerDescriptor desc;

   public ControllerResolver(ControllerDescriptor desc) throws NullPointerException
   {
      if (desc == null)
      {
         throw new NullPointerException("No null application descriptor accepted");
      }

      //
      List<ControllerMethod> methods = desc.getMethods();

      //
      this.desc = desc;
      this.methods = methods.toArray(new ControllerMethod[methods.size()]);
   }

   /**
    * Resolves a controller for a phase, a method id and a set of parameter names. The algorithm attempts to resolve
    * a single value with the following algorithm:
    *
    * <ul>
    *    <li>Filter any controller method that doesn't match the method id.</li>
    *    <li>When no method is retained, the null value is returned.</li>
    *    <li>When several methods are retained the resulting list is sorted according <i>resolution order</i>.
    *    If a first value is greater than all the others, this result is returned, otherwise a {@link AmbiguousResolutionException}
    *    is thrown.</li>
    * </ul>
    *
    * The <i>resolution order</i> uses three criteria for comparing two methods in the context of the specified parameter names.
    *
    * <ol>
    *    <li>The greater number of matched specified parameters.</li>
    *    <li>The lesser number of unmatched method arguments.</li>
    *    <li>The lesser number of unmatched method parameers.</li>
    * </ol>
    *
    * When the {@param methodId} is not specified the algorithm will be executed on a method set determined by:
    * <ul>
    *    <li>method with the name <i>index</i> are retained</li>
    *    <li>if a default controller is specified by the application and at least one <i>index</i> method exist
    *    on the default controller, any <i>index</i> method not on the default controller are discarded.</li>
    * </ul>
    * 
    * @param phase the phrase
    * @param methodId the method id
    * @param parameterNames the parameter names
    * @return the resolved controller method
    * @throws NullPointerException if the parameter names set is nul
    * @throws IllegalArgumentException if phase is not render when the method id is null
    * @throws AmbiguousResolutionException if more than a single result is found
    */
   public ControllerMethod resolve(Phase phase, String methodId, final Set<String> parameterNames) throws IllegalArgumentException, AmbiguousResolutionException
   {

      if (parameterNames == null)
      {
         throw new NullPointerException("No null parameter names accepted");
      }

      // todo : take in account multi valued parameters
      // todo : what happens with type conversion, somehow we should forbid m(String a) and m(int a)

      class Match implements Comparable<Match>
      {
         final ControllerMethod method;
         final int score1;
         final int score2;
         final int score3;
         Match(ControllerMethod method)
         {
            this.method = method;

            // The number of matched parameters
            HashSet<String> a = new HashSet<String>(parameterNames);
            a.retainAll(method.getArgumentNames());
            this.score1 = a.size();

            // The number of unmatched arguments
            a = new HashSet<String>(method.getArgumentNames());
            a.removeAll(parameterNames);
            this.score2 = a.size();

            // The number of unmatched parameters
            a = new HashSet<String>(parameterNames);
            a.removeAll(method.getArgumentNames());
            this.score3 = a.size();
         }
         public int compareTo(Match o)
         {
            int delta = o.score1 - score1;
            if (delta == 0)
            {
               delta = score2 - o.score2;
               if (delta == 0)
               {
                  delta = score3 - o.score3;
               }
            }
            return delta;
         }
         @Override
         public String toString()
         {
            return "Match[score1=" + score1 + ",score2=" + score2 + ",score3=" + score3 + ",method=" + method + "]";
         }
      }

      //
      List<Match> matches = new ArrayList<Match>();
      if (methodId == null)
      {
         if (phase != Phase.RENDER)
         {
            throw new IllegalArgumentException("Method id can only be null when the phase " + phase + " == " + Phase.RENDER);
         }
         else
         {
            for (ControllerMethod method : methods)
            {
               if (method.getPhase() == Phase.RENDER && method.getName().equals("index"))
               {
                  matches.add(new Match(method));
               }
            }
            for (int i = 0;i < matches.size();i++)
            {
               Match match = matches.get(i);
               if (match.method.getType() == desc.getDefault())
               {
                  ArrayList<Match> sub = new ArrayList<Match>();
                  for (int j = 0;j < matches.size();j++)
                  {
                     Match match2 = matches.get(j);
                     if (match2.method.getType() == desc.getDefault())
                     {
                        sub.add(match2);
                     }
                  }
                  matches = sub;
                  break;
               }
            }
         }
      }
      else
      {
         for (ControllerMethod method : methods)
         {
            if (method.getPhase() == phase && method.getId().equals(methodId))
            {
               matches.add(new Match(method));
            }
         }
      }

      //
      ControllerMethod found = null;
      if (matches.size() > 0)
      {
         Collections.sort(matches);
         Match first = matches.get(0);
         if (matches.size() > 1)
         {
            Match second = matches.get(1);
            if (first.compareTo(second) == 0)
            {
               throw new AmbiguousResolutionException("Two methods satisfies the index criteria: " +
                  first.method + " and " + second.method);
            }
         }
         found = first.method;
      }

      //
      return found;
   }
}
