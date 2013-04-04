/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.router;

import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.util.Collections;

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
    assertEquals(0, router.getSegmentNames().size());
    assertEquals(1, router.getPatternSize());
    PatternRoute patternRoute = router.getPattern(0);
    assertEquals("(?:([^/]+))(?:(?<=^)|(?=/)|$)", patternRoute.pattern.re.getPattern());
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
    assertEquals(0, router.getSegmentNames().size());
    assertEquals(1, router.getPatternSize());
    PatternRoute patternRoute = router.getPattern(0);
    assertEquals("(?:([^/]+))(?:(?<=^)|(?=/)|$)", patternRoute.pattern.re.getPattern());
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
    router.append("/{a}", Collections.singletonMap(Names.A, PathParam.matching(".*")));

    //
    assertEquals(0, router.getSegmentNames().size());
    assertEquals(1, router.getPatternSize());
    PatternRoute patternRoute = router.getPattern(0);
    assertEquals("(?:([^/]*))(?:(?<=^)|(?=/)|$)", patternRoute.pattern.re.getPattern());
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

    assertEquals(2, router.getSegmentSize("public"));
    Route publicRoute1 = router.getSegment("public", 0);
    assertEquals(1, publicRoute1.getSegmentSize("foo"));
    Route publicRoute2 = router.getSegment("public", 1);
    assertEquals(1, publicRoute2.getSegmentSize("bar"));
  }

  @Test
  public void testClear() throws Exception {
    Router router = new Router();
    Route foo = router.append("/foo");

    RouteMatch match = router.route("/foo");
    assertNotNull(match);
    assertSame(foo, match.getRoute());


    Route bar = foo.append("/bar");

    //
    /*RouteMatch*/ match = router.route("/foo/bar");
    assertNotNull(match);
    assertSame(bar, match.getRoute());

    //
    foo.clearChildren();
    match = router.route("/foo/bar");
    assertNull(match);
    match = router.route("/foo");
    assertNotNull(match);
    assertSame(foo, match.getRoute());
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
