/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package juzu.impl.plugin.controller;

import juzu.AmbiguousResolutionException;
import juzu.request.Phase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller method resolution algorithm.
 * <p/>
 * Resolves a controller method for a specified set of parameter names. The algorithm attempts to resolve a single value
 * with the following algorithm:
 * <p/>
 * <ul> <li>Filter the list of controllers, this is specific to the resolve method.</li> <li>When no method is retained,
 * the null value is returned.</li> <li>When several methods are retained the resulting list is sorted according
 * <i>resolution order</i>. If a first value is greater than all the others, this result is returned, otherwise a {@link
 * juzu.AmbiguousResolutionException} is thrown.</li> </ul>
 * <p/>
 * The <i>resolution order</i> uses three criteria for comparing two methods in the context of the specified parameter
 * names.
 * <p/>
 * <ol> <li>The greater number of matched specified parameters.</li> <li>The lesser number of unmatched method
 * arguments.</li> <li>The lesser number of unmatched method parameters.</li> <li>The default controller class.</li>
 * </ol>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class ControllerResolver<M> {

  // todo : take in account multi valued parameters
  // todo : what happens with type conversion, somehow we should forbid m(String a) and m(int a)

  public abstract M[] getMethods();

  public abstract String getId(M method);

  public abstract Phase getPhase(M method);

  public abstract String getName(M method);

  public abstract boolean isDefault(M method);

  public abstract Collection<String> getParameterNames(M method);

  private class Match implements Comparable<Match> {

    /** . */
    final M method;

    /** . */
    final int score1;

    /** . */
    final int score2;

    /** . */
    final int score3;

    /** . */
    final int score4;

    Match(Set<String> parameterNames, M method) {
      this.method = method;

      // The number of matched parameters
      HashSet<String> a = new HashSet<String>(parameterNames);
      a.retainAll(getParameterNames(method));
      this.score1 = a.size();

      // The number of unmatched arguments
      a = new HashSet<String>(getParameterNames(method));
      a.removeAll(parameterNames);
      this.score2 = a.size();

      // The number of unmatched parameters
      a = new HashSet<String>(parameterNames);
      a.removeAll(getParameterNames(method));
      this.score3 = a.size();

      // The default method
      this.score4 = isDefault(method) ? 0 : 1;
    }

    public int compareTo(Match o) {
      int delta = o.score1 - score1;
      if (delta == 0) {
        delta = score2 - o.score2;
        if (delta == 0) {
          delta = score3 - o.score3;
          if (delta == 0) {
            delta = score4 - o.score4;
          }
        }
      }
      return delta;
    }

    @Override
    public String toString() {
      return "Match[score1=" + score1 + ",score2=" + score2 + ",score3=" + score3 + ",score4=" + score4 + ",method=" + method + "]";
    }
  }

  /**
   * A method matches the filter when it has the render phase and the name <code>index</code>.
   *
   * @param parameterNames the parameter names
   * @return the resolved controller method
   * @throws NullPointerException if the parameter names set is nul
   * @throws juzu.AmbiguousResolutionException
   *                              if more than a single result is found
   */
  public final M resolve(Set<String> parameterNames) throws NullPointerException, AmbiguousResolutionException {
    if (parameterNames == null) {
      throw new NullPointerException("No null parameter names accepted");
    }

    //
    List<Match> matches = new ArrayList<Match>();
    for (M method : getMethods()) {
      if (getPhase(method) == Phase.VIEW && getName(method).equals("index")) {
        matches.add(new Match(parameterNames, method));
      }
    }

    //
    return select(matches);
  }

  /**
   * A method matches the filter when it matches the phase and the method id.
   *
   * @param phase          the phrase
   * @param methodId       the method id
   * @param parameterNames the parameter names
   * @return the resolved controller method
   * @throws NullPointerException if any parameter is nul
   * @throws juzu.AmbiguousResolutionException
   *                              if more than a single result is found
   */
  public final M resolve(Phase phase, String methodId, final Set<String> parameterNames) throws NullPointerException, AmbiguousResolutionException {
    if (parameterNames == null) {
      throw new NullPointerException("No null parameter names accepted");
    }
    if (phase == null) {
      throw new NullPointerException("Phase parameter cannot be null");
    }
    if (methodId == null) {
      throw new NullPointerException("Method id parameter cannot be null");
    }

    //
    List<Match> matches = new ArrayList<Match>();
    for (M method : getMethods()) {
      if (getPhase(method) == phase && getId(method).equals(methodId)) {
        matches.add(new Match(parameterNames, method));
      }
    }

    //
    return select(matches);
  }

  /**
   * A method matches the filter when it matches the type name, the method name and contains all the parameters.
   *
   * @param typeName       the optional type name
   * @param methodName     the method name
   * @param parameterNames the parameter names
   * @return the resolved controller method
   * @throws NullPointerException if the methodName or parameterNames argument is null
   * @throws juzu.AmbiguousResolutionException
   *                              if more than a single result is found
   */
  public M resolve(String typeName, String methodName, Set<String> parameterNames) throws NullPointerException, AmbiguousResolutionException {
    if (parameterNames == null) {
      throw new NullPointerException("No null parameter names accepted");
    }
    if (methodName == null) {
      throw new NullPointerException("Phase parameter cannot be null");
    }

    //
    List<Match> matches = new ArrayList<Match>();
    for (M method : getMethods()) {
      if (getParameterNames(method).containsAll(parameterNames)) {
        if (typeName == null) {
          if (getName(method).equals(methodName)) {
            matches.add(new Match(parameterNames, method));
          }
        }
        else {
          String id = typeName + "." + methodName;
          if (getId(method).equals(id)) {
            matches.add(new Match(parameterNames, method));
          }
        }
      }
    }

    //
    return select(matches);
  }

  private M select(List<Match> matches) throws AmbiguousResolutionException {
    M found = null;
    if (matches.size() > 0) {
      Collections.sort(matches);
      Match first = matches.get(0);
      if (matches.size() > 1) {
        Match second = matches.get(1);
        if (first.compareTo(second) == 0) {
          throw new AmbiguousResolutionException("Two methods satisfies the index criteria: " +
            first.method + " and " + second.method);
        }
      }
      found = first.method;
    }
    return found;
  }
}
