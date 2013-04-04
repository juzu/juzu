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

import juzu.impl.router.regex.RENode;
import juzu.impl.router.regex.REParser;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class FooRendererTestCase extends AbstractTestCase {

  private static void assertSatisfied(String expression, String... expected) throws Exception {
    RENode root = new REParser(expression).parse();
    List<ValueResolverFactory.Alternative> alternatives = new ValueResolverFactory().foo(root);
    assertEquals(expected.length, alternatives.size());
    for (int i = 0;i < expected.length;i++) {
      assertEquals(expected[i], alternatives.get(i).getResolvingExpression().toString());
    }
  }

  @Test
  public void testDisjunction() throws Exception {
    assertSatisfied("a|b", "a", "b");
  }

  @Test
  public void testAlternative() throws Exception {
    assertSatisfied("ab", "ab");
  }

  @Test
  public void testCharacterClassRange() throws Exception {
    assertSatisfied("[b-c]", "b");
  }

  @Test
  public void testCharacterClassOr() throws Exception {
    assertSatisfied("[bc]", "b");
  }

  @Test
  public void testCharacterClassAnd() throws Exception {
    assertSatisfied("[b&&b]", "b");
  }

  @Test
  public void testQuantifier() throws Exception {
    assertSatisfied("b{2,3}", "bb");
  }

  @Test
  public void testCharacterClassNegateChar() throws Exception {
    assertSatisfied("[^a]", " ");
  }

  @Test
  public void testCharacterClassNegateOr() throws Exception {
    assertSatisfied("[^ !]", "\"");
  }

  @Test
  public void testCharacterClassNegateAnd() throws Exception {
    assertSatisfied("[^a&&b]", " ");
  }

  @Test
  public void testAny() throws Exception {
    assertSatisfied(".", "a");
  }

  @Test
  public void testNonCapturingGroup() throws Exception {
    assertSatisfied("(?:a|b)", "a");
  }

  @Test
  public void testGroup() throws Exception {
    assertSatisfied("(a|b)", "(a|b)");
  }

  @Test
  public void testComplex() throws Exception {
    assertSatisfied("[a-z&&f-t&&p-q]", "p");
  }
}
