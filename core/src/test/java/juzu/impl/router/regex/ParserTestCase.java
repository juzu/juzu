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
