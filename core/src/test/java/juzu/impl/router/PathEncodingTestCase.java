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

import org.junit.Test;

import java.util.Collections;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PathEncodingTestCase extends AbstractControllerTestCase {

  @Test
  public void testSegment1() throws Exception {
//    Router router = router().add(route("/?")).build();
//    assertEquals("/%3F", router.render(Collections.<QualifiedName, String>emptyMap()));
  }

  @Test
  public void testSegment2() throws Exception {
//    Router router = router().add(route("/?{p}?")).build();
//    assertEquals("/%3Fa%3F", router.render(Collections.singletonMap(Names.P, "a")));
  }

  @Test
  public void testSegment3() throws Exception {
//    Router router = router().add(route("/{p}")).build();
//    assertEquals("/%C2%A2", router.render(Collections.singletonMap(Names.P, "\u00A2")));
  }

  @Test
  public void testParamDefaultForm() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/{<.+>p}");

    // Route
    router.assertRoute(Collections.singletonMap(Names.P, "/"), "/_");
    router.assertRoute(Collections.singletonMap(Names.P, "_"), "/%5F");
    router.assertRoute(Collections.singletonMap(Names.P, "_/"), "/%5F_");
    router.assertRoute(Collections.singletonMap(Names.P, "/_"), "/_%5F");
    router.assertRoute(Collections.singletonMap(Names.P, "?"), "/%3F");

    // Render
    assertEquals("/_", router.render(Collections.singletonMap(Names.P, "/")));
    assertEquals("/%5F", router.render(Collections.singletonMap(Names.P, "_")));
    assertEquals("/%5F_", router.render(Collections.singletonMap(Names.P, "_/")));
    assertEquals("/_%5F", router.render(Collections.singletonMap(Names.P, "/_")));
    assertEquals("/%3F", router.render(Collections.singletonMap(Names.P, "?")));
  }

  @Test
  public void testAlternativeSepartorEscape() throws Exception {
    RouterAssert router = new RouterAssert(':');
    router.append("/{<.+>p}");

    // Route
    router.assertRoute(Collections.singletonMap(Names.P, "/"), "/:");
    router.assertRoute(Collections.singletonMap(Names.P, "_"), "/_");
    router.assertRoute(Collections.singletonMap(Names.P, ":"), "/%3A");

    // Render
    assertEquals("/:", router.render(Collections.singletonMap(Names.P, "/")));
    assertEquals("/_", router.render(Collections.singletonMap(Names.P, "_")));
    assertEquals("/%3A", router.render(Collections.singletonMap(Names.P, ":")));
  }

  @Test
  public void testBug() throws Exception {
    Router router = new Router();
    router.append("/{<[^_]+>p}");

    // This is a *known* bug
    assertNull(router.route("/_"));

    // This is expected
    assertEquals("/_", router.render(Collections.singletonMap(Names.P, "/")));

    // This is expected
    assertNull(router.route("/%5F"));
    assertEquals("", router.render(Collections.singletonMap(Names.P, "_")));
  }

  @Test
  public void testParamPreservePath() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/{<[^/]+>[p]p}");

    // Route
    router.assertRoute(Collections.singletonMap(Names.P, "_"), "/_");
    assertNull(router.route("//"));

    // Render
    assertEquals("", router.render(Collections.singletonMap(Names.P, "/")));
  }

  @Test
  public void testD() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/{</[a-z]+/[a-z]+/?>p}");

    // Route
    router.assertRoute(Collections.singletonMap(Names.P, "/platform/administrator"), "/_platform_administrator");
    router.assertRoute(Collections.singletonMap(Names.P, "/platform/administrator"), "/_platform_administrator/");
    router.assertRoute(Collections.singletonMap(Names.P, "/platform/administrator/"), "/_platform_administrator_");
    router.assertRoute(Collections.singletonMap(Names.P, "/platform/administrator/"), "/_platform_administrator_/");

    // Render
    assertEquals("/_platform_administrator", router.render(Collections.singletonMap(Names.P, "/platform/administrator")));
    assertEquals("/_platform_administrator_", router.render(Collections.singletonMap(Names.P, "/platform/administrator/")));
    assertEquals("", router.render(Collections.singletonMap(Names.P, "/platform/administrator//")));
  }

  @Test
  public void testWildcardPathParamWithPreservePath() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/{<.*>[p]p}");

    // Render
    assertEquals("/", router.render(Collections.singletonMap(Names.P, "")));
    assertEquals("//", router.render(Collections.singletonMap(Names.P, "/")));
    assertEquals("/a", router.render(Collections.singletonMap(Names.P, "a")));
    assertEquals("/a/b", router.render(Collections.singletonMap(Names.P, "a/b")));

    // Route
    router.assertRoute(Collections.singletonMap(Names.P, ""), "/");
    router.assertRoute(Collections.singletonMap(Names.P, "/"), "//");
    router.assertRoute(Collections.singletonMap(Names.P, "a"), "/a");
    router.assertRoute(Collections.singletonMap(Names.P, "a/b"), "/a/b");
  }

  @Test
  public void testWildcardParamPathWithDefaultForm() throws Exception {
    Router router = new Router();
    router.append("/{<.*>p}");

    //
    assertEquals("/_", router.render(Collections.singletonMap(Names.P, "/")));
  }

}
