package org.juzu.impl.utils;

import junit.framework.TestCase;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PackageMapTestCase extends TestCase
{

   public void testResolve()
   {
      PackageMap<String> pm = new PackageMap<String>();
      pm.putValue("foo", "foo_value");
      assertEquals("foo_value", pm.resolveValue("foo"));
      assertEquals("foo_value", pm.resolveValue("foo.bar"));
      assertEquals(null, pm.resolveValue("foobar"));
   }

   public void testResolvePrefix()
   {
      PackageMap<String> pm = new PackageMap<String>();
      pm.putValue("foo", "foo_value");
      pm.putValue("foo.bar", "foo_bar_value");
      assertEquals("foo_value", pm.resolveValue("foo"));
      assertEquals("foo_value", pm.resolveValue("foo.juu"));
      assertEquals("foo_bar_value", pm.resolveValue("foo.bar"));
      assertEquals("foo_bar_value", pm.resolveValue("foo.bar.juu"));
   }

   public void testResolveEmptyPackage()
   {
      PackageMap<String> pm = new PackageMap<String>();
      pm.putValue("", "value");
      assertEquals("value", pm.resolveValue(""));
      assertEquals("value", pm.resolveValue("foo"));
      assertEquals("value", pm.resolveValue("foo.bar"));
   }
}
