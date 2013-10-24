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

package juzu.arquillian;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * An helper class that provides helper methods for setting up an Arquillian based test for Juzu.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Helper {

  /**
   * Create a base servlet war file. The returned war file is configured for running the Juzu
   * servlet with Guice container.
   *
   * @return the base servlet deployment
   */
  public static WebArchive createBaseServletDeployment() {
    return createBaseServletDeployment("guice");
  }

  /**
   * Create a base servlet war file. The returned war file is configured for running the Juzu
   * servlet and the specified injector provider.
   *
   * @param injectorVendor the dependency injection vendor to use
   * @return the base servlet deployment
   */
  public static WebArchive createBaseServletDeployment(String injectorVendor) {
    return createBaseDeployment(null, "servlet/web.xml", injectorVendor);
  }

  /**
   * Create a portlet war file. The returned war file is configured for running the GateIn
   * embedded portlet container. The returned deployment will contain the class hierarchy in
   * the package of the specified <code>baseClass</code> argument.
   *
   * @param baseClasses the base classes
   * @return the portlet deployment
   */
  public static WebArchive createBasePortletDeployment(Class... baseClasses) {
    return createBasePortletDeployment((WebArchive)null, baseClasses);
  }

  /**
   * Create a portlet war file. The returned war file is configured for running the GateIn
   * embedded portlet container. The returned deployment will contain the class hierarchy in
   * the package of the specified <code>baseClass</code> argument.
   *
   * @param war the web archive to use
   * @param baseClasses the base classes
   * @return the portlet deployment
   */
  public static WebArchive createBasePortletDeployment(WebArchive war, Class... baseClasses) {
    return createBasePortletDeployment(war, "guice", baseClasses);
  }

  /**
   * Create a portlet war file. The returned war file is configured for running the GateIn
   * embedded portlet container. The returned deployment will contain the class hierarchy in
   * the package of the specified <code>baseClass</code> argument.
   *
   * @param injectorVendor the dependency injection vendor to use
   * @param baseClasses the base classes
   * @return the portlet deployment
   */
  public static WebArchive createBasePortletDeployment(String injectorVendor, Class... baseClasses) {
    return createBasePortletDeployment(null, injectorVendor, baseClasses);
  }

  /**
   * Create a portlet war file. The returned war file is configured for running the GateIn
   * embedded portlet container. The returned deployment will contain the class hierarchy in
   * the package of the specified <code>baseClass</code> argument.
   *
   * @param war the web archive to use
   * @param injectorVendor the dependency injection vendor to use
   * @param baseClasses the base classes
   * @return the portlet deployment
   */
  public static WebArchive createBasePortletDeployment(WebArchive war, String injectorVendor, Class... baseClasses) {
    war = createBaseDeployment(war, "portlet/web.xml", injectorVendor);
    addClasses(war, baseClasses);
    return war;
  }

  /**
   * Add the classes hierarchies in the web archive.
   *
   * @param war the archive
   * @param baseClasses the base classes
   */
  public static void addClasses(WebArchive war, Class... baseClasses) {
    for (Class<?> baseClass : baseClasses) {
      try {
        URL root = baseClass.getClassLoader().getResource(baseClass.getName().replace('.', '/') + ".class");
        if (root != null) {
          File f = new File(root.toURI()).getParentFile();
          StringBuilder path = new StringBuilder(baseClass.getPackage().getName().replace('.', '/'));
          add(war, f, path);
        }
      }
      catch (URISyntaxException e) {
        throw new AssertionError("Could not create portlet deployment for class " + baseClass.getName(), e);
      }
    }
  }

  private static void add(WebArchive war, File f, StringBuilder path) {
    if (f.isDirectory()) {
      File[] children = f.listFiles();
      if (children != null) {
        for (File child : children) {
          int length = path.length();
          path.append('/').append(child.getName());
          add(war, child, path);
          path.setLength(length);
        }
      }
    } else {
      war.addAsResource(f, path.toString());
    }
  }

  private static WebArchive createBaseDeployment(WebArchive war, String webXMLPath, String injectorProvider) {
    if (war == null) {
      war = ShrinkWrap.create(WebArchive.class);
    }
    byte[] buffer = new byte[512];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    InputStream in = Helper.class.getResourceAsStream(webXMLPath);
    if (in == null) {
      throw new AssertionError("Could not locate " + webXMLPath + " web.xml for juzu testing");
    }
    try {
      for (int l = in.read(buffer);l != -1;l = in.read(buffer)) {
        baos.write(buffer, 0, l);
      }
    }
    catch (IOException e) {
      throw new AssertionError("Could not find read " + webXMLPath + " web.xml for juzu testing");
    }
    finally {
      try {
        in.close();
      }
      catch (Throwable ignore) {
      }
    }
    String webXML = baos.toString();
    webXML = String.format(webXML, injectorProvider);
    war.setWebXML(new StringAsset(webXML));
    return war;
  }
}
