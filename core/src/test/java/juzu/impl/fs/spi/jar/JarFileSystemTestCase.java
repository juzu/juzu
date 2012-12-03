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

package juzu.impl.fs.spi.jar;

import juzu.impl.common.Tools;
import juzu.impl.fs.Visitor;
import juzu.test.AbstractTestCase;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import javax.inject.Inject;
import javax.portlet.Portlet;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarFile;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JarFileSystemTestCase extends AbstractTestCase {


  @Test
  public void testMissing() throws Exception {
    URL url = Portlet.class.getProtectionDomain().getCodeSource().getLocation();
    JarFile jar = new JarFile(new File(url.toURI()));
    JarFileSystem fs = new JarFileSystem(jar);

    //
    Set<String> set = Tools.set(fs.getChildren(""));
    HashSet<String> expectedSet = Tools.set("META-INF/", "javax/");
    assertEquals(expectedSet, set);

    //
    Set<String> set2 = Tools.set(fs.getChildren("META-INF/"));
    HashSet<String> expectedSet2 = Tools.set("META-INF/MANIFEST.MF");
    assertEquals(expectedSet2, set2);

    //
    Set<String> set3 = Tools.set(fs.getChildren("javax/"));
    HashSet<String> expectedSet3 = Tools.set("javax/portlet/");
    assertEquals(expectedSet3, set3);

    //
    assertEquals("javax/", fs.getChild("", "javax"));
    assertEquals("javax/portlet/", fs.getChild("javax/", "portlet"));
    assertEquals("javax/portlet/Portlet.class", fs.getChild("javax/portlet/", "Portlet.class"));

    //
    Set<String> set4 = Tools.set(fs.getChildren("javax/portlet/filter/"));
    Set<String> expectedSet4 = new HashSet<String>();
    expectedSet4.add("javax/portlet/filter/EventFilter.class");
    expectedSet4.add("javax/portlet/filter/PortletRequestWrapper.class");
    expectedSet4.add("javax/portlet/filter/FilterChain.class");
    expectedSet4.add("javax/portlet/filter/ResourceRequestWrapper.class");
    expectedSet4.add("javax/portlet/filter/FilterConfig.class");
    expectedSet4.add("javax/portlet/filter/RenderResponseWrapper.class");
    expectedSet4.add("javax/portlet/filter/PortletResponseWrapper.class");
    expectedSet4.add("javax/portlet/filter/ActionResponseWrapper.class");
    expectedSet4.add("javax/portlet/filter/RenderRequestWrapper.class");
    expectedSet4.add("javax/portlet/filter/ActionFilter.class");
    expectedSet4.add("javax/portlet/filter/package.html");
    expectedSet4.add("javax/portlet/filter/RenderFilter.class");
    expectedSet4.add("javax/portlet/filter/EventRequestWrapper.class");
    expectedSet4.add("javax/portlet/filter/ResourceFilter.class");
    expectedSet4.add("javax/portlet/filter/PortletFilter.class");
    expectedSet4.add("javax/portlet/filter/ResourceResponseWrapper.class");
    expectedSet4.add("javax/portlet/filter/ActionRequestWrapper.class");
    expectedSet4.add("javax/portlet/filter/EventResponseWrapper.class");
    assertEquals(expectedSet4, set4);
  }

/*
  @Test
  public void testFoo() throws Exception {
    URL url = Test.class.getProtectionDomain().getCodeSource().getLocation();
    System.out.println("url = " + url);
    JarFile file = new JarFile(new File(url.toURI()));
    final JarFileSystem filesystem = new JarFileSystem(file);
    filesystem.traverse(new Visitor.Default<JarPath>() {
      @Override
      public void enterDir(JarPath dir, String name) throws IOException {
      }
    });
  }

  @Test
  public void testBaseURL() throws Exception {
    URL url = Test.class.getProtectionDomain().getCodeSource().getLocation();
    JarFile file = new JarFile(new File(url.toURI()));
    JarFileSystem filesystem = new JarFileSystem(file);
    URL baseURL = filesystem.getURL();
    assertNotNull(baseURL);
  }
*/
}
