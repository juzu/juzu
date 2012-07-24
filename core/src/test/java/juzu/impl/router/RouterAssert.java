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

package juzu.impl.router;

import junit.framework.Assert;
import juzu.impl.common.QualifiedName;
import juzu.impl.router.regex.REFactory;
import juzu.test.AbstractTestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouterAssert extends Router {

  public RouterAssert() throws RouterConfigException {
  }

  public RouterAssert(char separatorEscape) throws RouterConfigException {
    super(separatorEscape);
  }

  public RouterAssert(char separatorEscape, REFactory regexFactory) throws RouterConfigException {
    super(separatorEscape, regexFactory);
  }

  private ArrayList<Map<QualifiedName, String>> foo(String path, Map<String, String> parameters) {

    // Convert
    HashMap<String, String[]> tmp = new HashMap<String, String[]>();
    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      tmp.put(entry.getKey(), new String[]{entry.getValue()});
    }

    //
    Iterator<RouteMatch> result = matcher(path, tmp);

    //
    ArrayList<Map<QualifiedName, String>> a = new ArrayList<Map<QualifiedName, String>>();
    while (result.hasNext()) {
      HashMap<QualifiedName, String> actual = new HashMap<QualifiedName, String>();
      for (Map.Entry<Param, String> entry : result.next().getMatched().entrySet()) {
        actual.put(entry.getKey().getName(), entry.getValue());
      }
      a.add(actual);
    }

    //
    return a;
  }

  public void assertRoute(Map<QualifiedName, String> expected, String path, Map<String, String> parameters) {
    ArrayList<Map<QualifiedName, String>> b  = foo(path, parameters);
    if (b.isEmpty()) {
      throw AbstractTestCase.failure("Was expecting at least one result for " + path + " " + parameters);
    }
    Assert.assertFalse(b.isEmpty());
    AbstractTestCase.assertEquals(Collections.singletonList(expected), b.subList(0, 1));
  }

  public void assertRoute(Map<QualifiedName, String> expected, String path) {
    assertRoute(expected, path, Collections.<String, String>emptyMap());
  }

  public void assertRoutes(List<Map<QualifiedName, String>> expected, String path, Map<String, String> parameters) {
    AbstractTestCase.assertEquals(expected, foo(path, parameters));
  }

  public void assertRoutes(List<Map<QualifiedName, String>> expected, String path) {
    AbstractTestCase.assertEquals(expected, foo(path, Collections.<String, String>emptyMap()));
  }
}
