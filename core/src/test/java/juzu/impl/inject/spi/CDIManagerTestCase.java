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

import juzu.impl.inject.Export;
import juzu.impl.fs.spi.ram.RAMDir;
import juzu.impl.fs.spi.ram.RAMFileSystem;
import juzu.impl.fs.spi.ram.RAMPath;
import juzu.test.CompilerAssert;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CDIManagerTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public CDIManagerTestCase(InjectImplementation di) {
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
      foo.addFile("Bean2.java").update("package foo; @" + Export.class.getName() + " public class Bean2 {}");
      foo.addFile("Bean3.java").update("package foo; @" + Export.class.getName() + " public class Bean3 {}");
      foo.addFile("Bean4.java").update("package foo; @" + Export.class.getName() + " public class Bean4 {}");

      //
      CompilerAssert<RAMPath, RAMPath> helper = new CompilerAssert<RAMPath, RAMPath>(sources, classes);
      helper.assertCompile();
      URLClassLoader classLoader = new URLClassLoader(new URL[]{classes.getURL()}, Thread.currentThread().getContextClassLoader());

      //
      Class bean1Class = classLoader.loadClass("foo.Bean1");
      Class bean2Class = classLoader.loadClass("foo.Bean2");
      Class bean3Class = classLoader.loadClass("foo.Bean3");
      Class bean4Class = classLoader.loadClass("foo.Bean4");

      //
      init(classes, classLoader);
      bootstrap.declareBean(bean2Class, null, null, null);
      bootstrap.bindBean(bean3Class, null, bean3Class.newInstance());
      boot();

      //
      B bean1 = mgr.resolveBean(bean1Class);
      assertNotNull(bean1);
      B bean2 = mgr.resolveBean(bean2Class);
      assertNotNull(bean2);
      B bean3 = mgr.resolveBean(bean3Class);
      assertNotNull(bean3);
      B bean4 = mgr.resolveBean(bean4Class);
      assertNull(bean4);
    }
  }
}
