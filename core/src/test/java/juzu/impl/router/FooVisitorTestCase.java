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

import juzu.impl.router.regex.RENode;
import juzu.impl.router.regex.REParser;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class FooVisitorTestCase extends AbstractTestCase {

  private static void assertSatisfied(String expression, String... expected) throws Exception {
    RENode root = new REParser(expression).parse();
    root.accept(new CaptureGroupTransformation());
    List<ValueResolverFactory.Alternative> alternatives = new ValueResolverFactory().foo(root);
    assertEquals(expected.length, alternatives.size());
    for (int i = 0;i < expected.length;i++) {
      assertEquals(expected[i], alternatives.get(i).getResolvingExpression().toString());
    }
  }

  @Test
  public void testDisjunctions() throws Exception {
    assertSatisfied("a|.", "(a)", "(.)");
    assertSatisfied("a(.)b|.", "a(.)b", "(.)");
  }

  @Test
  public void testDisjunction() throws Exception {
    assertSatisfied("(?:a|b)(.)b", "a(.)b");
  }

  @Test
  public void testAlternative() throws Exception {
    assertSatisfied("a(.)b", "a(.)b");
  }

  @Test
  public void testCharacterClassRange() throws Exception {
    assertSatisfied("a(.)[b-c]", "a(.)b");
  }

  @Test
  public void testCharacterClassOr() throws Exception {
    assertSatisfied("a(.)[bc]", "a(.)b");
  }

  @Test
  public void testCharacterClassAnd() throws Exception {
    assertSatisfied("a(.)[b&&b]", "a(.)b");
  }

  @Test
  public void testQuantifier() throws Exception {
    assertSatisfied("a(.)b{2,3}", "a(.)bb");
  }

  @Test
  public void testCharacterClassNegateChar() throws Exception {
    assertSatisfied("a(.)[^a]", "a(.) ");
  }

  @Test
  public void testCharacterClassNegateOr() throws Exception {
    assertSatisfied("a(.)[^ !]", "a(.)\"");
  }

  @Test
  public void testCharacterClassNegateAnd() throws Exception {
    assertSatisfied("a(.)[^a&&b]", "a(.) ");
  }

  @Test
  public void testAny() throws Exception {
    assertSatisfied("a(.).", "a(.)a");
  }

  @Test
  public void testComplex() throws Exception {
    assertSatisfied("a(.)[a-z&&f-t&&p-q]", "a(.)p");
  }
}
