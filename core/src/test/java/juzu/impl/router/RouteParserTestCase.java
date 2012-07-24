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

import juzu.impl.router.RouteParser;
import juzu.impl.router.RouteParserHandler;
import juzu.impl.router.regex.SyntaxException;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteParserTestCase extends AbstractTestCase {

  
  static class Collector implements RouteParserHandler {

    /** . */
    private final List<String> chunks = new ArrayList<String>();

    /** . */
    private final StringBuilder buffer = new StringBuilder();

    public void openSegment() {
      buffer.append("/");
    }

    public void segmentChunk(CharSequence s, int from, int to) {
      buffer.append(s, from, to);
    }

    public void closeSegment() {
      chunks.add(buffer.toString());
      buffer.setLength(0);
    }

    public void closePath() {
      chunks.add("$");
    }

    public void query() {
      chunks.add("?");
    }

    public void queryParamLHS(CharSequence s, int from, int to) {
      buffer.append(s, from, to);
    }

    public void queryParamRHS(CharSequence s, int from, int to) {
      buffer.append(s, from, to);
    }

    public void queryParamRHS() {
      buffer.append('=');
    }

    public void openExpr() {
      buffer.append('{');
    }

    public void pattern(CharSequence s, int from, int to) {
      buffer.append('<');
      buffer.append(s, from, to);
      buffer.append('>');
    }

    public void modifiers(CharSequence s, int from, int to) {
      buffer.append('[');
      buffer.append(s, from, to);
      buffer.append(']');
    }

    public void ident(CharSequence s, int from, int to) {
      buffer.append(s, from, to);
    }

    public void closeExpr() {
      buffer.append('}');
    }

    public void endQueryParam() {
      chunks.add(buffer.toString());
      buffer.setLength(0);
    }
  }
  
  private List<String> parse(String route) {
    try {
      Collector collector = new Collector();
      RouteParser.parse(route, collector);
      return collector.chunks;
    }
    catch (SyntaxException e) {
      throw failure(e);
    }
  }

  private void fail(String route, int expectedCode, int index) {
    try {
      RouteParser.parse(route, new Collector());
      throw failure("Was expecting route to fail at " + index);
    }
    catch (SyntaxException e) {
      assertEquals(expectedCode, e.getCode());
      assertNotNull(e.getLocation());
      assertEquals(index, e.getLocation().getCol());
      assertEquals(1, e.getLocation().getLine());
    }
  }

  @Test
  public void testSimple() {
    assertEquals(Arrays.asList("$"), parse(""));
    assertEquals(Arrays.asList("/a", "$"), parse("a"));
    assertEquals(Arrays.asList("/a", "$"), parse("/a"));
    assertEquals(Arrays.asList("/a", "$"), parse("a/"));
    assertEquals(Arrays.asList("/a", "$"), parse("//a"));
    assertEquals(Arrays.asList("/a", "$"), parse("a//"));
    assertEquals(Arrays.asList("/a", "/b", "$"), parse("a/b"));
  }
  
  @Test
  public void testPathParam() {
    assertEquals(Arrays.asList("/{a}", "$"), parse("{a}"));
    assertEquals(Arrays.asList("/a{b}c", "$"), parse("a{b}c"));
  }

  @Test
  public void testQuery() {
    assertEquals(Arrays.asList("$", "?"), parse("?"));
    assertEquals(Arrays.asList("$", "?", "a"), parse("?a"));
    assertEquals(Arrays.asList("$", "?", "a="), parse("?a="));
    assertEquals(Arrays.asList("$", "?", "a=", "b"), parse("?a=&b"));
    assertEquals(Arrays.asList("$", "?", "a", "b"), parse("?a&b"));
    assertEquals(Arrays.asList("$", "?", "a", "b="), parse("?a&b="));
    assertEquals(Arrays.asList("$", "?", "a=b"), parse("?a=b"));
    assertEquals(Arrays.asList("$", "?", "a=b", "c"), parse("?a=b&c"));
    assertEquals(Arrays.asList("$", "?", "a={b}"), parse("?a={b}"));
    assertEquals(Arrays.asList("$", "?", "a={<b>c}"), parse("?a={<b>c}"));
    assertEquals(Arrays.asList("$", "?", "a={[]c}"), parse("?a={[]c}"));
    assertEquals(Arrays.asList("$", "?", "a={[b]c}"), parse("?a={[b]c}"));
  }

  @Test
  public void testInvalid() {
    fail("??", RouteParser.CODE_INVALID_QUESTION_MARK_CHAR, 2);
    fail("?&", RouteParser.CODE_INVALID_AMPERSAND_CHAR, 2);
    fail("?=", RouteParser.CODE_INVALID_EQUALS_CHAR, 2);
    fail("{", RouteParser.CODE_UNCLOSED_EXPR, 2);
    fail("{a", RouteParser.CODE_UNCLOSED_EXPR, 3);
    fail("?a={", RouteParser.CODE_UNCLOSED_EXPR, 5);
    fail("?a={b", RouteParser.CODE_UNCLOSED_EXPR, 6);
    fail("{<>", RouteParser.CODE_EMPTY_REGEX, 3);
    fail("?a={<>", RouteParser.CODE_EMPTY_REGEX, 6);
    fail("{<", RouteParser.CODE_UNCLOSED_REGEX, 3);
    fail("?a={<", RouteParser.CODE_UNCLOSED_REGEX, 6);
    fail("{<b>}", RouteParser.CODE_MISSING_EXPR_IDENT, 5);
    fail("{<b>", RouteParser.CODE_MISSING_EXPR_IDENT, 5);
    fail("?a={<b>}", RouteParser.CODE_MISSING_EXPR_IDENT, 8);
    fail("?a={<b>", RouteParser.CODE_MISSING_EXPR_IDENT, 8);
    fail("{[", RouteParser.CODE_UNCLOSED_MODIFIER, 3);
    fail("?a={[", RouteParser.CODE_UNCLOSED_MODIFIER, 6);
  }
}
