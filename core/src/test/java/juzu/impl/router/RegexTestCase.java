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

package juzu.impl.router;

import juzu.impl.router.regex.JRegexFactory;
import juzu.impl.router.regex.RE;
import juzu.test.AbstractTestCase;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RegexTestCase extends AbstractTestCase {

  @Test
  public void testLiteral() {
    RE regex = JRegexFactory.INSTANCE.compile("abc");
    RE.Match[] matches = regex.matcher().find("abc");
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getStart());
    assertEquals(3, matches[0].getEnd());
    assertEquals("abc", matches[0].getValue());
  }

  @Test
  public void testSimpleGroup1() {
    RE regex = JRegexFactory.INSTANCE.compile("(abc)");
    RE.Match[] matches = regex.matcher().find("abc");
    assertEquals(2, matches.length);
    assertEquals(0, matches[0].getStart());
    assertEquals(3, matches[0].getEnd());
    assertEquals("abc", matches[0].getValue());
    assertEquals(0, matches[1].getStart());
    assertEquals(3, matches[1].getEnd());
    assertEquals("abc", matches[1].getValue());
  }

  @Test
  public void testSimpleGroup2() {
    RE regex = JRegexFactory.INSTANCE.compile("a(b)c");
    RE.Match[] matches = regex.matcher().find("abc");
    assertEquals(2, matches.length);
    assertEquals(0, matches[0].getStart());
    assertEquals(3, matches[0].getEnd());
    assertEquals("abc", matches[0].getValue());
    assertEquals(1, matches[1].getStart());
    assertEquals(2, matches[1].getEnd());
    assertEquals("b", matches[1].getValue());
  }

  @Test
  public void testNonCapturingGroup() {
    RE regex = JRegexFactory.INSTANCE.compile("a(?:b)c");
    RE.Match[] matches = regex.matcher().find("abc");
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getStart());
    assertEquals(3, matches[0].getEnd());
    assertEquals("abc", matches[0].getValue());
  }
}
