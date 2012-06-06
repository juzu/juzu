package juzu.impl.utils;

import junit.framework.Assert;
import org.junit.Test;
import juzu.test.AbstractTestCase;

import java.util.Arrays;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PathTestCase
{

   @Test
   public void testIAE()
   {
      assertIAE(".");
      assertIAE(".a");
      assertIAE("a.");
      assertIAE("ab.");
      assertIAE("/.a");
      assertIAE("a/.b");
      assertIAE("a/b.");
      assertIAE("a/bc.");
   }

   @Test
   public void testParseName()
   {
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

   private void assertIAE(String path)
   {
      try
      {
         Path.parse(path);
         throw AbstractTestCase.failure("Was expecting parsing of " + path + " to throw an IAE");
      }
      catch (IllegalArgumentException e)
      {
         // Ok
      }
   }

   private void assertPath(boolean absolute, String[] names, String name, String extension, String test)
   {
      Path path = Path.parse(test);
      Assert.assertEquals(absolute, path.isAbsolute());
      Assert.assertEquals(Arrays.asList(names), Tools.list(path.getQN()));
      Assert.assertEquals(name, path.getRawName());
      Assert.assertEquals(extension, path.getExt());
   }

}
