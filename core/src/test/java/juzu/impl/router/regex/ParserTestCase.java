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

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ParserTestCase extends AbstractTestCase {

  @Test
  public void testSomeStuff() {
    Pattern pattern = Pattern.compile("^[\\^a\b]$");
    assertTrue(pattern.matcher("^").matches());
    assertTrue(pattern.matcher("a").matches());

  }

  private static class ParserTester {
    private final REParser parser;

    private ParserTester(CharSequence s) {
      this.parser = new REParser(s);
    }

    ParserTester assertParseCharacterClass(String expectedValue) {
      try {
        RENode node = parser.parseExpression();
        assertTrue(node instanceof RENode.CharacterClass);
        assertEquals(expectedValue, node.toString());
        return this;
      }
      catch (SyntaxException e) {
        throw failure(e);
      }
    }

    ParserTester assertParseDisjunction(String expectedValue) {
      try {
        RENode.Disjunction disjunction = parser.parseDisjunction();
        assertTrue(parser.isDone());
        if (expectedValue.length() == 0) {
          assertNull(disjunction);
        }
        else {
          assertNotNull(disjunction);
          assertEquals(expectedValue, disjunction.toString());
        }
        return this;
      }
      catch (SyntaxException e) {
        throw failure(e);
      }
    }

    ParserTester assertNotParseDisjunction() {
      int expectedIndex = parser.getIndex();
      try {
        parser.parseDisjunction();
        fail();
      }
      catch (SyntaxException e) {
        assertEquals(expectedIndex, parser.getIndex());
      }
      return this;
    }

    ParserTester assertParseExpression(String expectedValue, int expectedIndex) {
      try {
        RENode.Expr exp = parser.parseExpression();
        assertEquals(expectedValue, exp.toString());
        assertEquals(expectedIndex, parser.getIndex());
        return this;
      }
      catch (SyntaxException e) {
        throw failure(e);
      }
    }

    ParserTester assertNotParseExpression() {
      try {
        RENode.Expr expr = parser.parseExpression();
        assertNull(expr);
      }
      catch (SyntaxException e) {
      }
      return this;
    }

    ParserTester assertParseQuantifier(Quantifier expectedQuantifier) {
      Quantifier quantifier;
      try {
        quantifier = parser.parseQuantifier();
      }
      catch (SyntaxException e) {
        throw failure(e);
      }
      if (expectedQuantifier != null) {
        assertEquals(expectedQuantifier, quantifier);
      }
      else {
        assertNull(quantifier);
      }
      return this;
    }
  }

  public void testDisjunction() {
    new ParserTester("a|").assertParseDisjunction("<c>a</c>|");
    new ParserTester("|a").assertParseDisjunction("|<c>a</c>");
    new ParserTester("a|b").assertParseDisjunction("<c>a</c>|<c>b</c>");
  }

  public void testExtendedRegexp() {
    new ParserTester("").assertParseDisjunction("");
    new ParserTester(".").assertParseDisjunction("<./>");
    new ParserTester("^").assertParseDisjunction("<^/>");
    new ParserTester("^$").assertParseDisjunction("<^/><$/>");
    new ParserTester("a").assertParseDisjunction("<c>a</c>");
    new ParserTester("a|b").assertParseDisjunction("<c>a</c>|<c>b</c>");
    new ParserTester("a|b|c").assertParseDisjunction("<c>a</c>|<c>b</c>|<c>c</c>");
    new ParserTester("a+|b*").assertParseDisjunction("<+><c>a</c></+>|<*><c>b</c></*>");
    new ParserTester("\\.").assertParseDisjunction("<c>.</c>");
  }

  public void testExpression() {
    new ParserTester("").assertNotParseExpression();
    new ParserTester("^").assertParseExpression("<^/>", 1);
    new ParserTester("^+").assertParseExpression("<+><^/></+>", 2);
    new ParserTester("$").assertParseExpression("<$/>", 1);
    new ParserTester("$+").assertParseExpression("<+><$/></+>", 2);
    new ParserTester("a").assertParseExpression("<c>a</c>", 1);
    new ParserTester("a+").assertParseExpression("<+><c>a</c></+>", 2);
    new ParserTester(".").assertParseExpression("<./>", 1);
    new ParserTester(".+").assertParseExpression("<+><./></+>", 2);
    new ParserTester("\\+").assertParseExpression("<c>+</c>", 2);
    new ParserTester("\\++").assertParseExpression("<+><c>+</c></+>", 3);
    new ParserTester("*").assertNotParseExpression();
    new ParserTester("+").assertNotParseExpression();
    new ParserTester("?").assertNotParseExpression();
    new ParserTester("{").assertNotParseExpression();
    new ParserTester("|").assertNotParseExpression();
  }

  public void testGroup() {
    new ParserTester("(a)").assertParseExpression("<(><c>a</c></(>", 3);
    new ParserTester("(a(b)c)").assertParseExpression("<(><c>a</c><(><c>b</c></(><c>c</c></(>", 7);
    new ParserTester("(?:a)").assertParseExpression("<(?:><c>a</c></(?:>", 5);
    new ParserTester("(?=a)").assertParseExpression("<(?=><c>a</c></(?=>", 5);
    new ParserTester("(?!a)").assertParseExpression("<(?!><c>a</c></(?!>", 5);
    new ParserTester("(?<=a)").assertParseExpression("<(?<=><c>a</c></(?<=>", 6);
    new ParserTester("(?<!a)").assertParseExpression("<(?<!><c>a</c></(?<!>", 6);
    new ParserTester("(?)").assertParseExpression("<(><c>?</c></(>", 3);
    new ParserTester("(?_)").assertNotParseExpression();
    new ParserTester("(?<_)").assertNotParseExpression();
  }

  // missing stuff:
  // escape in bracket


  public void testQuantifier() {
    new ParserTester("*").assertParseQuantifier(Quantifier.zeroOrMore(Quantifier.Mode.GREEDY));
    new ParserTester("+").assertParseQuantifier(Quantifier.oneOrMore(Quantifier.Mode.GREEDY));
    new ParserTester("?").assertParseQuantifier(Quantifier.onceOrNotAtAll(Quantifier.Mode.GREEDY));
    new ParserTester("*a").assertParseQuantifier(Quantifier.zeroOrMore(Quantifier.Mode.GREEDY));
    new ParserTester("+a").assertParseQuantifier(Quantifier.oneOrMore(Quantifier.Mode.GREEDY));
    new ParserTester("?a").assertParseQuantifier(Quantifier.onceOrNotAtAll(Quantifier.Mode.GREEDY));
    new ParserTester("*?").assertParseQuantifier(Quantifier.zeroOrMore(Quantifier.Mode.RELUCTANT));
    new ParserTester("+?").assertParseQuantifier(Quantifier.oneOrMore(Quantifier.Mode.RELUCTANT));
    new ParserTester("??").assertParseQuantifier(Quantifier.onceOrNotAtAll(Quantifier.Mode.RELUCTANT));
    new ParserTester("*+").assertParseQuantifier(Quantifier.zeroOrMore(Quantifier.Mode.POSSESSIVE));
    new ParserTester("++").assertParseQuantifier(Quantifier.oneOrMore(Quantifier.Mode.POSSESSIVE));
    new ParserTester("?+").assertParseQuantifier(Quantifier.onceOrNotAtAll(Quantifier.Mode.POSSESSIVE));
    new ParserTester("a").assertParseQuantifier(null);
    new ParserTester("").assertParseQuantifier(null);
    new ParserTester("{2}").assertParseQuantifier(Quantifier.exactly(Quantifier.Mode.GREEDY, 2));
    new ParserTester("{2,}").assertParseQuantifier(Quantifier.atLeast(Quantifier.Mode.GREEDY, 2));
    new ParserTester("{2,4}").assertParseQuantifier(Quantifier.between(Quantifier.Mode.GREEDY, 2, 4));
  }

  public void testParseBracketExpression() {
    new ParserTester("[a]").assertParseCharacterClass("[a]");
    new ParserTester("[^a]").assertParseCharacterClass("[^[a]]");
    new ParserTester("[^a-b]").assertParseCharacterClass("[^[a-b]]");
    new ParserTester("[a-b]").assertParseCharacterClass("[a-b]");
    new ParserTester("[ab]").assertParseCharacterClass("[[a]||[b]]");
    new ParserTester("[a&]").assertParseCharacterClass("[[a]||[&]]");
    new ParserTester("[a&&b]").assertParseCharacterClass("[[a]&&[b]]");
    new ParserTester("[a&&[^b]]").assertParseCharacterClass("[[a]&&[^[b]]]");
    new ParserTester("[a[^b]]").assertParseCharacterClass("[[a]||[^[b]]]");
    new ParserTester("[a[b]]").assertParseCharacterClass("[[a]||[b]]");
    new ParserTester("[a[b]c]").assertParseCharacterClass("[[a]||[[b]||[c]]]");
    new ParserTester("[[a]bc]").assertParseCharacterClass("[[a]||[[b]||[c]]]");
    new ParserTester("[-]").assertParseCharacterClass("[-]");
    new ParserTester("[a-]").assertParseCharacterClass("[[a]||[-]]");
//      new ParserTester("[---]").assertParseCharacterClass("[---]");
    new ParserTester("[#--]").assertParseCharacterClass("[#--]");
  }

}
