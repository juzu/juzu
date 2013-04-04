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

package juzu.impl.router.parser;

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

    public void segmentOpen() {
      buffer.append("/");
    }

    public void segmentChunk(CharSequence s, int from, int to) {
      buffer.append(s, from, to);
    }

    public void segmentClose() {
      chunks.add(buffer.toString());
      buffer.setLength(0);
    }

    public void pathClose(boolean slash) {
      if (slash) {
        chunks.add("/$");
      } else {
        chunks.add("$");
      }
    }

    public void query() {
      chunks.add("?");
    }

    public void exprOpen() {
      buffer.append('{');
    }

    public void exprIdent(CharSequence s, int from, int to) {
      buffer.append(s, from, to);
    }

    public void exprClose() {
      buffer.append('}');
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

    assertEquals(Arrays.asList("/a", "$"), parse("//a"));
    assertEquals(Arrays.asList("/a", "/b", "$"), parse("a/b"));
  }
  
  @Test
  public void testPathParam() {
    assertEquals(Arrays.asList("/{a}", "$"), parse("{a}"));
    assertEquals(Arrays.asList("/a{b}c", "$"), parse("a{b}c"));
  }

  @Test
  public void testEndWithSeparator() {
    assertEquals(Arrays.asList("/$"), parse("/"));
    assertEquals(Arrays.asList("/a", "/$"), parse("a/"));
    assertEquals(Arrays.asList("/a", "/$"), parse("a//"));
    assertEquals(Arrays.asList("/a", "/$"), parse("/a/"));
    assertEquals(Arrays.asList("/a", "/$"), parse("/a//"));
  }

  @Test
  public void testInvalid() {
    fail("{", RouteParser.CODE_UNCLOSED_EXPR, 2);
    fail("{a", RouteParser.CODE_UNCLOSED_EXPR, 3);
    fail("{}", RouteParser.CODE_MISSING_EXPR_IDENT, 2);
  }
}
