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
import juzu.impl.common.CharStream;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RegExpParserTestCase extends AbstractTestCase {

  @Test
  public void testParseDisjunction() {
    parseParse("|");
    parseParse("a|");
    parseParse("|a");
    parseParse("a|b");
  }

  @Test
  public void testParseAlternative() {
    parseParse("ab");
    parseParse("^a$");
  }

  @Test
  public void testParseAssertion() {
    parseParse("^");
    parseParse("$");
  }

  @Test
  public void testParseAny() {
    parseParse(".");
  }

  @Test
  public void testParseCharacterLiteral() {
    parseParse("a");
    parseParse("-");
    parseParse("]");
    parseParse("\\$");
    parseParse("\\00");
    parseParse("\\01");
    parseParse("\\018");
    parseParse("\\011");
    parseParse("\\0311");
    parseParse("\\x00");
    parseParse("\\xFF");
    parseParse("\\xff");
    parseParse("\\u0000");
    parseParse("\\uFFFF");

    //
    failFail("\\");
    failFail("\\k");
    failFail("\\0");
    failFail("\\08");
    failFail("\\x1");
    failFail("\\x1G");
    failFail("\\u1");
    failFail("\\u12");
    failFail("\\u123");
    failFail("\\u123G");
  }

  @Test
  public void testCharacterClass() throws Exception {
    parseParse("[a]");
    parseParse("[{]");
    parseParse("[a{]");
    parseParse("[]a]");
    parseParse("[[a]]");
    parseParse("[a[b]]");

    //
    failFail("[a");
    failFail("[]");
  }

  @Test
  public void testCharacterClassNegation() throws Exception {
    parseParse("[^a]");
    parseParse("[^]a]");
    parseParse("[^[a]]");
    parseParse("[^a[b]]");

    //
    failFail("[^a");
    failFail("[]");
  }

  @Test
  public void testCharacterClassRange() {
    parseParse("[a-b]");
    parseParse("[-]");
    parseParse("[ --]");
    parseParse("[ --b]");
    parseParse("[--/]");
    parseParse("[a-]");
    parseParse("[---]");
    parseParse("[--]");

    //
    parseFail("[--[ab]]"); // Parse - or a or b
  }

  @Test
  public void testCharacterClassAlternative() {
    parseParse("[&]");
    parseParse("[a&&b]");
    parseParse("[a&&]");
    parseParse("[a&&[b]]");

    //
    failFail("[&&]");
    failFail("[&&&]");
    failFail("[&&&&]");

    //
    parseFail("[&&b]");
  }

  @Test
  public void testCharacterClassEscape() {
    parseParse("[\\\\]");
    parseParse("[\\[]");
    parseParse("[\\]]");
    parseParse("[\\.]");
    parseParse("[\\-]");

    //
    failFail("[\\k]");
  }

  @Test
  public void testCharacterClassAny() {
    parseParse("[.]");
    parseParse("[^.]");
  }

  @Test
  public void testCharacterClassAssert() {
    parseParse("[$]");
    parseParse("[^$]");
    parseParse("[^^]");
    parseParse("[$^]");
  }

  @Test
  public void testParseGroup() {
    parseParse("()");
    parseParse("(?)");
    parseParse("(a)");
    parseParse("(|)");
    parseParse("(a|)");
    parseParse("(|a)");
    parseParse("(a|b)");
    parseParse("(()())");

    //
    parseParse("(?:)");
    parseParse("(?=)");
    parseParse("(?!)");
    parseParse("(?<=)");
    parseParse("(?<!)");

    //
    failFail("(?a)");
    failFail("(");
    failFail(")");
    failFail("(?<)");
    failFail("(?<a)");
  }

  @Test
  public void testParseQuantifier() {
    parseParse("^?");
    parseParse("$?");
    parseParse("a?");
    parseParse("()?");
    parseParse("[a]?");
    parseParse("^*");
    parseParse("$*");
    parseParse("a*");
    parseParse("()*");
    parseParse("[a]*");
    parseParse("^+");
    parseParse("$+");
    parseParse("a+");
    parseParse("()+");
    parseParse("[a]+");
    parseParse("a{0}");
    parseParse("a{0,}");
    parseParse("a{0,1}");

    //
    failFail("?");
    failFail("+");
    failFail("*");
    failFail("{");
    failFail("a{");
    failFail("a{}");
    failFail("a{b");
    failFail("a{0");
    failFail("a{0,");
    failFail("a{0,1");
  }

  @Test
  public void testParseQuantifierMode() {
    parseParse("a??");
    parseParse("a?+");
    parseParse("a+?");
    parseParse("a++");
    parseParse("a*?");
    parseParse("a*+");
    parseParse("a{0}?");
    parseParse("a{0}+");
  }

  void parseFail(String s) {
    parse(s, false, true);
  }

  void parseParse(String s) {
    parse(s, false, false);
  }

  void failFail(String s) {
    parse(s, true, true);
  }

  void parse(String s, boolean javaFail, boolean javaccFail) {
    try {
      Pattern.compile(s);
      if (javaFail) {
        throw new AssertionFailedError("Was expecting " + s + " to not be compilable");
      }
    }
    catch (PatternSyntaxException e) {
      if (!javaFail) {
        AssertionFailedError afe = new AssertionFailedError("Was expecting " + s + " to be compilable");
        afe.initCause(e);
        throw afe;
      }
    }
    try {
      CharStream stream = new CharStream(s);
      Lexer lexer = new Lexer(stream);
      REParser parser = new REParser(lexer);
      parser.parse();
      assertEquals(s.length(), stream.getIndex());
      if (lexer.hasNext()) {
        throw new SyntaxException();
      }
      if (javaccFail) {
        throw new AssertionFailedError("Was expecting " + s + " to not be compilable");
      }
    }
    catch (SyntaxException e) {
      if (!javaccFail) {
        AssertionFailedError afe = new AssertionFailedError("Was expecting " + s + " to be compilable");
        afe.initCause(e);
        throw afe;
      }
    }
  }
}
