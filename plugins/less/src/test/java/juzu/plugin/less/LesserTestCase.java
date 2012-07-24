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

package juzu.plugin.less;

import juzu.plugin.less.impl.lesser.Compilation;
import juzu.plugin.less.impl.lesser.Failure;
import juzu.plugin.less.impl.lesser.JSR223Context;
import juzu.plugin.less.impl.lesser.LessError;
import juzu.plugin.less.impl.lesser.Lesser;
import juzu.plugin.less.impl.lesser.URLLessContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(Parameterized.class)
public class LesserTestCase {

  @Parameterized.Parameters
  public static Collection<Object[]> configs() throws Exception {
    return Arrays.asList(new Object[][]{{new Lesser(new JSR223Context())}});
  }

  /** . */
  private Lesser lesser;

  public LesserTestCase(Lesser lesser) {
    this.lesser = lesser;
  }

  @Test
  public void testSimple() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/test/"));
    Compilation compilation = (Compilation)lesser.compile(context, "simple.less");
    Assert.assertEquals(".class {\n" +
      "  width: 2;\n" +
      "}\n", compilation.getValue());

    //
    compilation = (Compilation)lesser.compile(context, "simple.less", true);
    Assert.assertEquals(".class{width:2;}\n", compilation.getValue());
  }

  @Test
  public void testFail() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/test/"));
    Failure ret = (Failure)lesser.compile(context, "fail.less");
    LinkedList<LessError> errors = ret.getErrors();
    Assert.assertEquals(1, errors.size());
    LessError error = errors.get(0);
    Assert.assertEquals("fail.less", error.src);
    Assert.assertEquals(1, error.line);
    Assert.assertEquals(8, error.column);
    Assert.assertEquals(8, error.index);
    Assert.assertEquals("Parse", error.type);
  }

  @Test
  public void testCannotResolveImport() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/test/"));
    Failure failure = (Failure)lesser.compile(context, "cannotresolveimport.less");
    LinkedList<LessError> errors = failure.getErrors();
    Assert.assertEquals(1, errors.size());
    LessError error = errors.get(0);
    Assert.assertEquals(1, error.line);
    Assert.assertEquals(4, error.column);
    Assert.assertEquals(4, error.index);
    Assert.assertEquals(Collections.emptyList(), Arrays.asList(error.extract));
    Assert.assertEquals("Parse", error.type);
  }

  @Test
  public void testSeveralErrors() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/test/"));
    Failure failure = (Failure)lesser.compile(context, "severalerrors1.less");
    LinkedList<LessError> errors = failure.getErrors();
    Assert.assertEquals(2, errors.size());
  }

  @Test
  public void testBootstrap() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/bootstrap/"));
    long time = -System.currentTimeMillis();
    Compilation compilation = (Compilation)lesser.compile(context, "bootstrap.less");
    time += System.currentTimeMillis();
    Assert.assertNotNull(compilation);
    System.out.println("Bootstrap parsed in " + time + "ms");
  }

  @Test
  public void testImport() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/test/"));
    Compilation compilation = (Compilation)lesser.compile(context, "importer.less");
    Assert.assertEquals("a {\n" +
      "  width: 2px;\n" +
      "}\n", compilation.getValue());
  }

  @Test
  public void testUnresolableVariable() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/test/"));
    Failure failure = (Failure)lesser.compile(context, "unresolvablevariable.less");
    LinkedList<LessError> errors = failure.getErrors();
    Assert.assertEquals(1, errors.size());
    LessError error = errors.get(0);
    Assert.assertEquals(1, error.line);
    Assert.assertEquals(17, error.column);
    Assert.assertEquals(17, error.index);
    Assert.assertEquals("Name", error.type);
  }

  @Test
  public void testExtract() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/test/"));
    Failure failure = (Failure)lesser.compile(context, "extract.less");
    Assert.assertEquals(1, failure.getErrors().size());
    LessError error = failure.getErrors().get(0);
    Assert.assertEquals(2, error.line);
    String[] extract = error.extract;
    Assert.assertEquals(3, extract.length);
    Assert.assertEquals("// comment 1", extract[0]);
    Assert.assertEquals("a { width: + 1px }", extract[1]);
    Assert.assertEquals("// comment 2", extract[2]);
  }

  @Test
  public void testImportRelative() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/test/"));
    Compilation compilation = (Compilation)lesser.compile(context, "relative.less");
    Assert.assertEquals("a {\n" +
        "  width: 2px;\n" +
        "}\n", compilation.getValue());
  }
}
