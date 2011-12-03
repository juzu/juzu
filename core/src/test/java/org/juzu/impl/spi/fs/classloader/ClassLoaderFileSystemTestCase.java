/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.juzu.impl.spi.fs.classloader;

import junit.framework.TestCase;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.juzu.impl.utils.Tools;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ClassLoaderFileSystemTestCase extends TestCase
{

   public void testFoo() throws Exception
   {
      File f = File.createTempFile("test", ".jar");
      f.deleteOnExit();
      JavaArchive jar = ShrinkWrap.create(JavaArchive.class);
      jar.addAsResource(new StringAsset("bar.txt_value"), "bar.txt");
      jar.addAsResource(new StringAsset("foo/bar.txt_value"), "foo/bar.txt");
      jar.addAsResource(new StringAsset("foo/bar/juu.txt_value"), "foo/bar/juu.txt");
      jar.as(ZipExporter.class).exportTo(f, true);

      //
      ClassLoader cl = new URLClassLoader(new URL[]{f.toURI().toURL()}, ClassLoader.getSystemClassLoader());
      ClassLoaderFileSystem fs = new ClassLoaderFileSystem(cl);

      //
      String foo = fs.getPath("foo");
      assertEquals("foo/", foo);
      assertEquals("foo", fs.getName(foo));
      assertEquals("foo", fs.packageOf(foo, '.', new StringBuilder()).toString());
      assertEquals(Arrays.asList("foo/bar.txt"), Tools.list(fs.getChildren(foo)));

      //
      String fooBar = fs.getPath("foo", "bar.txt");
      assertEquals("foo/bar.txt", fooBar);
      assertEquals("bar.txt", fs.getName(fooBar));
      assertEquals("foo", fs.packageOf(fooBar, '.', new StringBuilder()).toString());

      //
      String fooBarJuu = fs.getPath("foo", "bar", "juu.txt");
      assertEquals("foo/bar/juu.txt", fooBarJuu);
      assertEquals("juu.txt", fs.getName(fooBarJuu));
      assertEquals("foo.bar", fs.packageOf(fooBarJuu, '.', new StringBuilder()).toString());

      //
      assertEquals(null, fs.getPath("juu"));
   }
}
