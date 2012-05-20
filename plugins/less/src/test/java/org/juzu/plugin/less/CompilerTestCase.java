package org.juzu.plugin.less;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.juzu.plugin.less.impl.lesser.Compilation;
import org.juzu.plugin.less.impl.lesser.Failure;
import org.juzu.plugin.less.impl.lesser.JSR223Context;
import org.juzu.plugin.less.impl.lesser.Lesser;
import org.juzu.plugin.less.impl.lesser.URLLessContext;

import java.util.Arrays;
import java.util.Collection;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(Parameterized.class)
public class CompilerTestCase
{

   @Parameterized.Parameters
   public static Collection<Object[]> configs() throws Exception
   {
      return Arrays.asList(new Object[][]{{new Lesser(new JSR223Context())}});
   }

   /** . */
   private Lesser lesser;

   public CompilerTestCase(Lesser lesser)
   {
      this.lesser = lesser;
   }

   @Test
   public void testSimple() throws Exception
   {
      URLLessContext context = new URLLessContext(CompilerTestCase.class.getClassLoader().getResource("lesser/test/"));
      Compilation ret = (Compilation)lesser.parse(context, "simple.less");
      Assert.assertEquals(".class {\n" +
         "  width: 2;\n" +
         "}\n", ret.getValue());

      //
      ret = (Compilation)lesser.parse(context, "simple.less", true);
      Assert.assertEquals(".class{width:2;}\n", ret.getValue());
   }

   @Test
   public void testFail() throws Exception
   {
      URLLessContext context = new URLLessContext(CompilerTestCase.class.getClassLoader().getResource("lesser/test/"));
      Failure failure = (Failure)lesser.parse(context, "fail.less");
      Assert.assertEquals(1, failure.line);
      Assert.assertEquals(8, failure.column);
      Assert.assertEquals(8, failure.index);
   }

   @Test
   public void testBootstrap() throws Exception
   {
      URLLessContext context = new URLLessContext(CompilerTestCase.class.getClassLoader().getResource("lesser/bootstrap/"));
      long time = - System.currentTimeMillis();
      Object ret = lesser.parse(context, "bootstrap.less");
      time += System.currentTimeMillis();
      Assert.assertNotNull(ret);
      System.out.println("parsed in " + time + "ms");
   }
}
