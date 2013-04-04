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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class REParser {

  /** . */
  private final Lexer lexer;

  public REParser(CharSequence seq) {
    this.lexer = new Lexer(seq);
  }

  public REParser(Lexer lexer) {
    this.lexer = lexer;
  }

  public void reset() {
    lexer.reset();
  }

  public int getIndex() {
    return lexer.getIndex();
  }

  public RENode parse() throws SyntaxException {
    return parseDisjunction();
  }

  public boolean isDone() {
    return lexer.isDone();
  }

  public RENode.Disjunction parseDisjunction() throws SyntaxException {
    RENode.Alternative alternative = parseAlternative();
    if (alternative != null) {
      if (lexer.next(Kind.OR)) {
        RENode.Disjunction next = parseDisjunction();
        return new RENode.Disjunction(alternative, next);
      }
      else {
        return new RENode.Disjunction(alternative);
      }
    }
    else {
      if (lexer.next(Kind.OR)) {
        RENode.Disjunction next = parseDisjunction();
        return new RENode.Disjunction(null, next);
      }
      else {
        return null;
      }
    }
  }

  public RENode.Alternative parseAlternative() throws SyntaxException {
    RENode.Expr expr = parseExpression();
    if (expr != null) {
      RENode.Alternative next = parseAlternative();
      return new RENode.Alternative(expr, next);
    }
    else {
      return null;
    }
  }

  public RENode.Expr parseExpression() throws SyntaxException {
    RENode.Expr expr;
    if (lexer.next(Kind.BEGIN)) {
      expr = new RENode.Assertion.Begin();
    }
    else if (lexer.next(Kind.END)) {
      expr = new RENode.Assertion.End();
    }
    else if (lexer.next(Kind.GROUP_OPEN)) {
      GroupType groupType = GroupType.forPrefix(lexer.getToken());
      RENode.Disjunction group = parseDisjunction();
      if (lexer.next(Kind.GROUP_CLOSE)) {
        expr = new RENode.Group(group, groupType);
      }
      else {
        throw new SyntaxException("Group not closed ");
      }
    }
    else {
      expr = parseCharacter();
    }
    if (expr != null) {
      Quantifier quantifier = parseQuantifier();
      if (quantifier != null) {
        expr.setQuantifier(quantifier);
      }
    }
    return expr;
  }

  private static final Pattern QUANTIFIER_PATTERN = Pattern.compile(
      "^" +
          "(\\?|\\+|\\*)|\\{([0-9]+)(?:(,)([0-9]*))?\\}" +
          "$");

  public Quantifier parseQuantifier() throws SyntaxException {
    if (lexer.next(Kind.QUANTIFIER)) {
      String quantifierToken = lexer.getToken();
      Matcher matcher = QUANTIFIER_PATTERN.matcher(quantifierToken);
      if (!matcher.matches()) {
        throw new AssertionError("The quantifier token " + quantifierToken + " is not valid");
      }
      Quantifier.Range range;
      if (matcher.group(1) != null) {
        switch (quantifierToken.charAt(0)) {
          case '*':
            range = Quantifier.Range.zeroOrMore();
            break;
          case '+':
            range = Quantifier.Range.oneOrMore();
            break;
          case '?':
            range = Quantifier.Range.onceOrNotAtAll();
            break;
          default:
            throw new AssertionError();
        }
      }
      else {
        int min = Integer.parseInt(matcher.group(2));
        Integer max;
        if (matcher.group(3) != null) {
          max = matcher.group(4).isEmpty() ? null : Integer.parseInt(matcher.group(4));
        }
        else {
          max = min;
        }
        range = new Quantifier.Range(min, max);
      }
      Quantifier.Mode mode;
      if (lexer.next(Kind.QUANTIFIER_MODE)) {
        switch (lexer.getToken().charAt(0)) {
          case '?':
            mode = Quantifier.Mode.RELUCTANT;
            break;
          case '+':
            mode = Quantifier.Mode.POSSESSIVE;
            break;
          default:
            throw new AssertionError();
        }
      }
      else {
        mode = Quantifier.Mode.GREEDY;
      }
      return new Quantifier(mode, range);
    }
    else {
      return null;
    }
  }

  public RENode.Atom parseCharacter() throws SyntaxException {
    if (lexer.next(Kind.ANY)) {
      return new RENode.Any();
    }
    else {
      RENode.Atom atom = parseCharacterLiteral();
      if (atom == null) {
        atom = parseCharacterClass();
      }
      return atom;
    }
  }

  public RENode.Char parseCharacterLiteral() throws SyntaxException {
    if (lexer.next(Kind.LITERAL)) {
      return new RENode.Char(lexer.getToken().charAt(0));
    }
    else {
      return null;
    }
  }

  public RENode.CharacterClass parseCharacterClass() throws SyntaxException {
    RENode.CharacterClassExpr cce = _parseCharacterClass();
    if (cce != null) {
      return new RENode.CharacterClass(cce);
    }
    else {
      return null;
    }
  }

  private RENode.CharacterClassExpr _parseCharacterClass() throws SyntaxException {
    if (lexer.next(Kind.CC_OPEN)) {
      boolean negated = lexer.getToken().length() > 1;
      RENode.CharacterClassExpr expr = parseCharacterClassExpression();
      if (expr != null) {
        if (lexer.next(Kind.CC_CLOSE)) {
          return negated ? new RENode.CharacterClassExpr.Not(expr) : expr;
        }
        else {
          throw new SyntaxException("");
        }
      }
      else {
        throw new SyntaxException("");
      }
    }
    else {
      return null;
    }
  }

  public RENode.CharacterClassExpr parseCharacterClassExpression() throws SyntaxException {
    RENode.CharacterClassExpr left = parseCharacterClassTerm();
    if (left != null) {
      boolean and = lexer.next(Kind.CC_AND);
      RENode.CharacterClassExpr right = parseCharacterClassExpression();
      if (right != null) {
        if (and) {
          return new RENode.CharacterClassExpr.And(left, right);
        }
        else {
          return new RENode.CharacterClassExpr.Or(left, right);
        }
      }
      else {
        return left;
      }
    }
    else {
      return null;
    }
  }

  public RENode.CharacterClassExpr parseCharacterClassTerm() throws SyntaxException {
    RENode.CharacterClassExpr expr = _parseCharacterClass();
    if (expr == null) {
      RENode.CharacterClassExpr.Char c = parseCharacterClassLiteral();
      if (c != null) {
        if (lexer.next(Kind.HYPHEN)) {
          RENode.CharacterClassExpr.Char to = parseCharacterClassLiteral();
          if (to != null) {
            expr = new RENode.CharacterClassExpr.Range(c, to);
          }
          else {
            throw new SyntaxException();
          }
        }
        else {
          expr = c;
        }
      }
      else if (lexer.next(Kind.ANY)) {
        // NOT SURE THIS IS CORRECT
        expr = new RENode.CharacterClassExpr.Char('.');
      }
      else if (lexer.next(Kind.BEGIN)) {
        // NOT SURE THIS IS CORRECT
        expr = new RENode.CharacterClassExpr.Char('^');
      }
      else if (lexer.next(Kind.END)) {
        // NOT SURE THIS IS CORRECT
        expr = new RENode.CharacterClassExpr.Char('$');
      }
    }
    return expr;
  }

  public RENode.CharacterClassExpr.Char parseCharacterClassLiteral() throws SyntaxException {
    if (lexer.next(Kind.LITERAL)) {
      return new RENode.CharacterClassExpr.Char(lexer.getToken().charAt(0));
    }
    else {
      return null;
    }
  }
}
