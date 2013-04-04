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

import juzu.impl.router.regex.RENode;
import juzu.impl.router.regex.REParser;
import juzu.impl.router.regex.RERenderer;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RouteEscaperTestCase extends AbstractTestCase {

  private void match(String pattern, String test, String expectedValue) throws Exception {
    REParser parser = new REParser(pattern);
    RouteEscaper escaper = new RouteEscaper('/', '_');
    RENode.Disjunction re = parser.parseDisjunction();
    re.accept(escaper);
    Pattern p = Pattern.compile(RERenderer.render(re, new StringBuilder()).toString());
    Matcher matcher = p.matcher(test);
    assertTrue(matcher.find());
    assertEquals(expectedValue, matcher.group());
  }

  @Test
  public void testMatch() throws Exception {
    match(".*", "_", "_");
    match(".*", "_/", "_");
    match(".*", "_/_", "_");
    match("/", "_/", "_");
    match("/*", "_/_", "_");
    match("[/a]*", "_a_/_", "_a_");
    match("[,-1&&[^/]]*", "_/_", "");
  }

  @Test
  public void testGroup() throws Exception {
    match("(/)", "_", "_");
    match("(?:/)", "_", "_");
    match(".(?=/)", "a_", "a");
    match("a(?!/)", "ab", "a");
    match(".(?<=/)a", "ba_a", "_a");
    match(".(?<!/)a", "_aba", "ba");
  }
}
