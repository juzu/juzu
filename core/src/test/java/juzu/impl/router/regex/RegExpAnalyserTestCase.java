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

package juzu.impl.router.regex;

import juzu.test.AbstractTestCase;
import org.junit.Test;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RegExpAnalyserTestCase extends AbstractTestCase {

  private void assertAnalyse(String expectedPattern, String pattern) {
    try {
      RENode.Disjunction disjunction = new REParser(pattern).parseDisjunction();
      assertEquals(expectedPattern, RERenderer.render(disjunction, new StringBuilder()).toString());
    }
    catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testCharacterClass() {
    assertAnalyse("[a]", "[a]");
    assertAnalyse("[ab]", "[ab]");
    assertAnalyse("[ab]", "[a[b]]");
    assertAnalyse("[abc]", "[abc]");
    assertAnalyse("[abc]", "[[a]bc]");
    assertAnalyse("[abc]", "[a[b]c]");
    assertAnalyse("[abc]", "[ab[c]]");
    assertAnalyse("[abc]", "[[ab]c]");
    assertAnalyse("[abc]", "[a[bc]]");
    assertAnalyse("[abc]", "[[abc]]");
    assertAnalyse("[^a]", "[^a]");
  }

  @Test
  public void testGroupContainer() {
    assertAnalyse("(a)", "(a)");
    assertAnalyse("(a(?:b))", "(a(?:b))");
    assertAnalyse("(?:a(b))", "(?:a(b))");
    assertAnalyse("(a)(?:b)", "(a)(?:b)");
    assertAnalyse("(a(b))", "(a(b))");
    assertAnalyse("(a)(b)", "(a)(b)");

    //
    assertAnalyse("(?=a)", "(?=a)");
    assertAnalyse("(?!a)", "(?!a)");
    assertAnalyse("(?<=a)", "(?<=a)");
    assertAnalyse("(?<!a)", "(?<!a)");
  }

  @Test
  public void testBilto() {
    assertAnalyse("[a]+", "[a]+");
  }
}
