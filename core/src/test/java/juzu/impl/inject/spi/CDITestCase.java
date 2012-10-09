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

package juzu.impl.inject.spi;

import juzu.impl.fs.spi.ram.RAMDir;
import juzu.impl.fs.spi.ram.RAMFileSystem;
import juzu.impl.fs.spi.ram.RAMPath;
import juzu.test.CompilerAssert;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CDITestCase<B, I> extends AbstractInjectTestCase<B, I> {

  public CDITestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void testExport() throws Exception {
    if (di == InjectImplementation.CDI_WELD) {
      RAMFileSystem sources = new RAMFileSystem();
      RAMFileSystem classes = new RAMFileSystem();

      //
      RAMDir foo = sources.addDir(sources.getRoot(), "foo");
      foo.addFile("Bean1.java").update("package foo; public class Bean1 {}");
      foo.addFile("Bean2.java").update("package foo; public class Bean2 {}");
      foo.addFile("Bean3.java").update("package foo; public class Bean3 {}");

      //
      RAMDir juzu = sources.addDir(sources.getRoot(), "juzu");
      juzu.addFile("Bean1.java").update("package juzu; public class Bean1 {}");
      juzu.addFile("Bean2.java").update("package juzu; public class Bean2 {}");
      juzu.addFile("Bean3.java").update("package juzu; public class Bean3 {}");

      //
      CompilerAssert<RAMPath, RAMPath> helper = new CompilerAssert<RAMPath, RAMPath>(sources, classes);
      helper.assertCompile();
      URLClassLoader classLoader = new URLClassLoader(new URL[]{classes.getURL()}, Thread.currentThread().getContextClassLoader());

      //
      Class fooBean1Class = classLoader.loadClass("foo.Bean1");
      Class fooBean2Class = classLoader.loadClass("foo.Bean2");
      Class fooBean3Class = classLoader.loadClass("foo.Bean3");

      //
      Class juzuBean1Class = classLoader.loadClass("juzu.Bean1");
      Class juzuBean2Class = classLoader.loadClass("juzu.Bean2");
      Class juzuBean3Class = classLoader.loadClass("juzu.Bean3");

      //
      init(classes, classLoader);

      //
      bootstrap.declareBean(fooBean2Class, null, null, null);
      bootstrap.bindBean(fooBean3Class, null, fooBean3Class.newInstance());

      //
      bootstrap.declareBean(juzuBean2Class, null, null, null);
      bootstrap.bindBean(juzuBean3Class, null, juzuBean3Class.newInstance());

      //
      boot();

      //
      B fooBean1 = mgr.resolveBean(fooBean1Class);
      assertNotNull(fooBean1);
      B fooBean2 = mgr.resolveBean(fooBean2Class);
      assertNotNull(fooBean2);
      B fooBean3 = mgr.resolveBean(fooBean3Class);
      assertNotNull(fooBean3);

      //
      B juzuBean1 = mgr.resolveBean(juzuBean1Class);
      assertNull(juzuBean1);
      B juzuBean2 = mgr.resolveBean(juzuBean2Class);
      assertNotNull(juzuBean2);
      B juzuBean3 = mgr.resolveBean(juzuBean3Class);
      assertNotNull(juzuBean3);
    }
  }
}
