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

import juzu.impl.common.QualifiedName;
import org.junit.Test;

import java.util.Collections;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RenderTestCase extends AbstractControllerTestCase {

  @Test
  public void testRoot() throws Exception {
    Router router = new Router();
    router.append("/");

    //
    assertEquals("/", router.render(Collections.<QualifiedName, String>emptyMap()));
  }

  @Test
  public void testA() throws Exception {
    Router router = new Router();
    router.append("/a");

    //
    assertEquals("/a", router.render(Collections.<QualifiedName, String>emptyMap()));
  }

  @Test
  public void testAB() throws Exception {
    Router router = new Router();
    router.append("/a/b");

    //
    assertEquals("/a/b", router.render(Collections.<QualifiedName, String>emptyMap()));
  }

  @Test
  public void testPathParam() throws Exception {
    Router router = new Router();
    router.append("/{p}");

    //
    assertEquals("/a", router.render(Collections.singletonMap(Names.P, "a")));
    assertEquals("", router.render(Collections.<QualifiedName, String>emptyMap()));
  }

  @Test
  public void testSimplePatternPathParam() throws Exception {
    Router router = new Router();
    router.append("/{<a>p}");

    //
    assertEquals("/a", router.render(Collections.singletonMap(Names.P, "a")));
    assertEquals("", router.render(Collections.singletonMap(Names.P, "ab")));
  }

  @Test
  public void testPrecedence() throws Exception {
    Router router = new Router();
    router.append("/a");
    router.append("/{<a>p}/b");

    //
    assertEquals("/a", router.render(Collections.<QualifiedName, String>emptyMap()));

    //
    assertEquals("/a/b", router.render(Collections.singletonMap(Names.P, "a")));
  }

  @Test
  public void testLang() throws Exception {
    Router router = new Router();
    router.append("/{<(([A-Za-z]{2})/)?>[p]a}b");

    //
    assertEquals("/fr/b", router.render(Collections.singletonMap(Names.A, "fr/")));
    assertEquals("/b", router.render(Collections.singletonMap(Names.A, "")));
  }

  @Test
  public void testDisjunction() throws Exception {
    Router router = new Router();
    router.append("/{<a|b>a}");

    //
    assertEquals("/b", router.render(Collections.singletonMap(Names.A, "b")));
  }

  @Test
  public void testCaptureGroup() throws Exception {
    Router router = new Router();
    router.append("/{<a(.)c>[c]a}");

    //
    assertEquals("/abc", router.render(Collections.singletonMap(Names.A, "b")));
  }
}
