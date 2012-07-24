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

import juzu.UndeclaredIOException;
import juzu.impl.common.QualifiedName;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteMatchTestCase extends AbstractControllerTestCase {

  private void assertMatch(String expected, Route route, Map<QualifiedName, String> parameters) {
    RouteMatch match = route.matches(parameters);
    assertNotNull("Was expecting to match " + expected, match);
    StringBuilder sb = new StringBuilder();
    URIWriter writer = new URIWriter(sb);
    try {
      match.render(writer);
      for (Map.Entry<QualifiedName, String> entry : match.getUnmatched().entrySet()) {
        writer.appendQueryParameter(entry.getKey().getName(), entry.getValue());
      }
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }

  @Test
  public void testPath() {
    Router router = new Router();
    Route a = router.append("/a");
    RouteMatch match = a.matches(Collections.<QualifiedName, String>emptyMap());
    assertNotNull(match);
    assertMatch("/a?a=foo", a, Collections.<QualifiedName, String>singletonMap(Names.A, "foo"));
  }

  @Test
  public void testPathParam() {
    Router router = new Router();
    Route a = router.append("/{a}");
    assertNull(a.matches(Collections.<QualifiedName, String>emptyMap()));
    assertMatch("/foo", a, Collections.<QualifiedName, String>singletonMap(Names.A, "foo"));
  }
}
