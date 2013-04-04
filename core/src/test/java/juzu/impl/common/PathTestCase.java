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

package juzu.impl.common;

import junit.framework.Assert;
import juzu.test.AbstractTestCase;
import org.junit.Test;
import static junit.framework.Assert.*;

import java.util.Arrays;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PathTestCase {

  @Test
  public void testNameValidator() {
    assertTrue(Lexers.NAME_VALIDATOR.matcher("a").matches());
    assertTrue(Lexers.NAME_VALIDATOR.matcher("ab").matches());
    assertTrue(Lexers.NAME_VALIDATOR.matcher("a.b").matches());
    assertFalse(Lexers.NAME_VALIDATOR.matcher(".").matches());
    assertFalse(Lexers.NAME_VALIDATOR.matcher(".a").matches());
    assertFalse(Lexers.NAME_VALIDATOR.matcher("a.").matches());
  }

  @Test
  public void testParseIAE() {
    assertIAE(".");
    assertIAE(".a");
    assertIAE("a.");
    assertIAE("ab.");
    assertIAE("/.a");
    assertIAE("a/.b");
    assertIAE("a/b.");
    assertIAE("a/bc.");
    assertIAE("..");
  }

  @Test
  public void testParseName() {
    assertPath(false, new String[]{}, "", null, "");
    assertPath(true, new String[]{}, "", null, "/");
    assertPath(true, new String[]{}, "", null, "//");

    assertPath(false, new String[]{}, "a", null, "a");
    assertPath(true, new String[]{}, "a", null, "/a");
    assertPath(true, new String[]{}, "a", null, "//a");
    assertPath(false, new String[]{"a"}, "", null, "a/");

    assertPath(false, new String[]{}, "a", "b", "a.b");
    assertPath(true, new String[]{}, "a", "b", "/a.b");
    assertPath(false, new String[]{"a"}, "b", null, "a/b");
    assertPath(false, new String[]{"a"}, "b", null, "a//b");
    assertPath(false, new String[]{"a"}, "b", "c", "a/b.c");
    assertPath(true, new String[]{"a"}, "b", "c", "/a/b.c");
  }

  @Test
  public void testAppend() {
    assertIAE("a", "b./c");
    assertIAE("a", ".b/c");
    assertIAE("a", "..b/c");
    assertIAE("a", ".b./c");
    assertIAE("a", "b../c");
    assertIAE("a", "bb./c");
    assertIAE("a", "b.b/c");
    assertIAE("a", ".bb/c");
    assertIAE("a", ".../c");
  }

  @Test
  public void testAppendIAE() {
    assertPath(false, new String[]{}, "b", null, Path.parse("").append("b"));
    assertPath(false, new String[]{}, "b", "c", Path.parse("").append("b.c"));
    assertPath(false, new String[]{}, "b", null, Path.parse("").append("./b"));
    assertPath(false, new String[]{}, "c", null, Path.parse("a/b").append("../c"));
    assertPath(false, new String[]{}, "b", null, Path.parse("").append("a/../b"));
    assertPath(false, new String[]{}, "b", "c", Path.parse("").append("b.c"));
    assertPath(false, new String[]{"a"}, "c", null, Path.parse("a/b").append("c"));
    assertPath(false, new String[]{"a"}, "d", null, Path.parse("a/b.c").append("d"));
  }

  private void assertIAE(String path) {
    try {
      Path.parse(path);
      throw AbstractTestCase.failure("Was expecting parsing of " + path + " to throw an IAE");
    }
    catch (IllegalArgumentException e) {
      // Ok
    }
  }

  private void assertIAE(String prefixPath, String path) {
    Path p = Path.parse(prefixPath);
    try {
      p.append(path);
      throw AbstractTestCase.failure("Was expecting parsing of " + path + " to throw an IAE");
    }
    catch (IllegalArgumentException e) {
      // Ok
    }
  }

  private void assertPath(boolean absolute, String[] names, String name, String extension, Path test) {
    Assert.assertEquals(absolute, test.isAbsolute());
    Iterable<String> qn = test.getDirs();
    Assert.assertNotNull(qn);
    Assert.assertEquals(Arrays.asList(names), Tools.list(qn));
    Assert.assertEquals(name, test.getRawName());
    Assert.assertEquals(extension, test.getExt());
  }

  private void assertPath(boolean absolute, String[] names, String name, String extension, String test) {
    assertPath(absolute, names, name, extension, Path.parse(test));
  }
}
