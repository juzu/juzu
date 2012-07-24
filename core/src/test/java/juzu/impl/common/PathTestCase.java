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

package juzu.impl.common;

import junit.framework.Assert;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.util.Arrays;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PathTestCase {

  @Test
  public void testParseIAE() {
    assertIAE(".");
    assertIAE(".a");
    assertIAE("a.");
    assertIAE("ab.");
    assertIAE("a.b.c");
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
    Assert.assertEquals(Arrays.asList(names), Tools.list(test.getQN()));
    Assert.assertEquals(name, test.getRawName());
    Assert.assertEquals(extension, test.getExt());
  }

  private void assertPath(boolean absolute, String[] names, String name, String extension, String test) {
    assertPath(absolute, names, name, extension, Path.parse(test));
  }
}
