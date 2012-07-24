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
