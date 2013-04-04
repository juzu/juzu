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

import junit.framework.AssertionFailedError;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RegExpRendererTestCase extends AbstractTestCase {

  static void assertRender(RENode re, String expected) {
    try {
      String rendered;
      if (re != null) {
        rendered = RERenderer.render(re, new StringBuilder()).toString();
      }
      else {
        rendered = "";
      }
      assertEquals(expected, rendered);
    }
    catch (IOException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  static RENode.Disjunction disjunction(String regexp) {
    try {
      REParser parser = new REParser(regexp);
      RENode.Disjunction re = parser.parseDisjunction();
      assertTrue(parser.isDone());
      return re;
    }
    catch (SyntaxException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  static RENode.Alternative alternative(String regexp) {
    try {
      REParser parser = new REParser(regexp);
      RENode.Alternative re = parser.parseAlternative();
      assertTrue(parser.isDone());
      return re;
    }
    catch (SyntaxException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  static RENode.Expr expression(String regexp) {
    try {
      REParser parser = new REParser(regexp);
      RENode.Expr re = parser.parseExpression();
      assertTrue(parser.isDone());
      return re;
    }
    catch (SyntaxException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  static RENode.CharacterClass characterClass(String regexp) {
    try {
      REParser parser = new REParser(regexp);
      RENode.CharacterClass re = parser.parseCharacterClass();
      assertTrue(parser.isDone());
      return re;
    }
    catch (SyntaxException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  @Test
  public void testSimple() {
    assertRender(expression("."), ".");
    assertRender(expression("^"), "^");
    assertRender(expression("\\."), "\\.");
  }

  @Test
  public void testDisjunction() {
    assertRender(disjunction(""), "");
    assertRender(disjunction("|"), "|");
    assertRender(disjunction("a|"), "a|");
    assertRender(disjunction("|a"), "|a");
    assertRender(disjunction("a|b"), "a|b");
  }

  @Test
  public void testAlternative() {
    assertRender(alternative(""), "");
    assertRender(alternative("ab"), "ab");
    assertRender(alternative("abc"), "abc");
  }

  @Test
  public void testCharacterClass() {
    assertRender(characterClass(""), "");
    assertRender(characterClass("[a]"), "[a]");
    assertRender(characterClass("[ab]"), "[ab]");
    assertRender(characterClass("[.\\]]"), "[\\.\\]]");
  }
}
