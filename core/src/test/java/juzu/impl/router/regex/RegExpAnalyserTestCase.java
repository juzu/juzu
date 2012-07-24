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
