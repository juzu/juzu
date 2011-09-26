package org.juzu.impl.compiler;

import junit.framework.TestCase;

import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ToolsTestCase extends TestCase
{

   public void testEmptyNoRecursePackageMatcher()
   {
      Pattern p = Tools.getPackageMatcher("", false);
      assertTrue(p.matcher("foo").matches());
      assertFalse(p.matcher("").matches());
      assertFalse(p.matcher("foo.bar").matches());
   }

   public void testEmptyRecursePackageMatcher()
   {
      Pattern p = Tools.getPackageMatcher("", true);
      assertTrue(p.matcher("foo").matches());
      assertFalse(p.matcher("").matches());
      assertTrue(p.matcher("foo.bar").matches());
   }

   public void testNoRecursePackageMatcher()
   {
      Pattern p = Tools.getPackageMatcher("foo", false);
      assertFalse(p.matcher("bar").matches());
      assertFalse(p.matcher("").matches());
      assertFalse(p.matcher("foo").matches());
      assertFalse(p.matcher("foobar").matches());
      assertTrue(p.matcher("foo.bar").matches());
      assertFalse(p.matcher("foo.bar.juu").matches());
   }

   public void testRecursePackageMatcher()
   {
      Pattern p = Tools.getPackageMatcher("foo", true);
      assertFalse(p.matcher("bar").matches());
      assertFalse(p.matcher("").matches());
      assertFalse(p.matcher("foo").matches());
      assertFalse(p.matcher("foobar").matches());
      assertTrue(p.matcher("foo.bar").matches());
      assertTrue(p.matcher("foo.bar.juu").matches());
   }
}
