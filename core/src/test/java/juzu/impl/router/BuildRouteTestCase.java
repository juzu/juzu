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

import juzu.test.AbstractTestCase;
import org.junit.Test;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class BuildRouteTestCase extends AbstractTestCase {

  @Test
  public void testParameterSegment() throws Exception {
    Router router = new Router();
    router.append("/{a}");

    //
    assertEquals(0, router.root.getSegmentNames().size());
    assertEquals(1, router.root.getPatternSize());
    PatternRoute patternRoute = router.root.getPattern(0);
    assertEquals("^/(?:([^/]+))(?:(?<=^/)|(?=/)|$)", patternRoute.pattern.re.getPattern());
    assertEquals(1, patternRoute.params.length);
    assertEquals(Names.A, patternRoute.params[0].name);
    assertEquals("^(.+)$", patternRoute.params[0].matchingRegex[0].re.getPattern());
    assertFalse(patternRoute.params[0].preservePath);
    assertEquals(2, patternRoute.chunks.length);
    assertEquals("", patternRoute.chunks[0]);
    assertEquals("", patternRoute.chunks[1]);
  }

  @Test
  public void testQualifiedParameterSegment() throws Exception {
    Router router = new Router();
    router.append("/{q:a}");

    //
    assertEquals(0, router.root.getSegmentNames().size());
    assertEquals(1, router.root.getPatternSize());
    PatternRoute patternRoute = router.root.getPattern(0);
    assertEquals("^/(?:([^/]+))(?:(?<=^/)|(?=/)|$)", patternRoute.pattern.re.getPattern());
    assertEquals(1, patternRoute.params.length);
    assertEquals(Names.Q_A, patternRoute.params[0].name);
    assertEquals("^(.+)$", patternRoute.params[0].matchingRegex[0].re.getPattern());
    assertFalse(patternRoute.params[0].preservePath);
    assertEquals(2, patternRoute.chunks.length);
    assertEquals("", patternRoute.chunks[0]);
    assertEquals("", patternRoute.chunks[1]);
  }

  @Test
  public void testPatternSegment() throws Exception {
    Router router = new Router();
    router.append("/{<.*>a}");

    //
    assertEquals(0, router.root.getSegmentNames().size());
    assertEquals(1, router.root.getPatternSize());
    PatternRoute patternRoute = router.root.getPattern(0);
    assertEquals("^/(?:([^/]*))(?:(?<=^/)|(?=/)|$)", patternRoute.pattern.re.getPattern());
    assertEquals(1, patternRoute.params.length);
    assertEquals(Names.A, patternRoute.params[0].name);
    assertEquals("^(.*)$", patternRoute.params[0].matchingRegex[0].re.getPattern());
    assertFalse(patternRoute.params[0].preservePath);
    assertEquals(2, patternRoute.chunks.length);
    assertEquals("", patternRoute.chunks[0]);
    assertEquals("", patternRoute.chunks[1]);
  }

  @Test
  public void testSamePrefix() throws Exception {
    Router router = new Router();
    router.append("/public/foo");
    router.append("/public/bar");

    assertEquals(2, router.root.getSegmentSize("public"));
    Route publicRoute1 = router.root.getSegment("public", 0);
    assertEquals(1, publicRoute1.getSegmentSize("foo"));
    Route publicRoute2 = router.root.getSegment("public", 1);
    assertEquals(1, publicRoute2.getSegmentSize("bar"));
  }

  private void assertEquals(Route expectedRoute, Route route) {
    assertEquals(expectedRoute.getClass(), route.getClass());
    assertEquals(expectedRoute.getSegmentNames(), route.getSegmentNames());
    for (String segmentName : expectedRoute.getSegmentNames()) {
      assertEquals(expectedRoute.getSegmentSize(segmentName), route.getSegmentSize(segmentName));
      for (int segmentIndex = 0;segmentIndex < expectedRoute.getSegmentSize(segmentName);segmentIndex++) {
        SegmentRoute expectedSegmentRoute = expectedRoute.getSegment(segmentName, segmentIndex);
        SegmentRoute segmentRoute = route.getSegment(segmentName, segmentIndex);
        assertEquals(expectedSegmentRoute, segmentRoute);
      }
    }
    assertEquals(expectedRoute.getPatternSize(), route.getPatternSize());
    for (int i = 0;i < expectedRoute.getPatternSize();i++) {
      assertEquals(expectedRoute.getPattern(i), route.getPattern(i));
    }
    if (route instanceof PatternRoute) {
      assertEquals(((PatternRoute)expectedRoute).pattern.toString(), ((PatternRoute)route).pattern.toString());
      assertEquals(((PatternRoute)expectedRoute).params.length, ((PatternRoute)route).params.length);
      for (int i = 0;i < ((PatternRoute)expectedRoute).params.length;i++) {
        PathParam expectedParam = ((PatternRoute)expectedRoute).params[i];
        PathParam param = ((PatternRoute)route).params[i];
        assertEquals(expectedParam.name, param.name);
        assertEquals(expectedParam.matchingRegex.length, param.matchingRegex.length);
        for (int j = 0;j < expectedParam.matchingRegex.length;j++) {
          assertEquals(expectedParam.matchingRegex[j].toString(), param.matchingRegex[j].toString());
        }
        assertEquals(expectedParam.preservePath, param.preservePath);
      }
    }
    else if (route instanceof SegmentRoute) {
      assertEquals(((SegmentRoute)expectedRoute).name, ((SegmentRoute)route).name);
    }
  }
}
