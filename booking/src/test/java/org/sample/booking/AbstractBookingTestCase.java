/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sample.booking;

import juzu.arquillian.Helper;
import juzu.impl.common.Name;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.url.URLFileSystem;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.sample.booking.qualifier.Authentication;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(Arquillian.class)
public abstract class AbstractBookingTestCase {

  public static WebArchive createDeployment() {
    WebArchive war = Helper.createBasePortletDeployment();
    war.addAsWebInfResource(new File("src/main/webapp/WEB-INF/portlet.xml"));

    //
    try {
      URL root = Flash.class.getClassLoader().getResource(Flash.class.getName().replace('.', '/') + ".class");
      if (root != null) {
        File f = new File(root.toURI()).getParentFile();
        LinkedList<String> path = new LinkedList<String>();
        for (String name : Name.create(Flash.class).getParent()) {
          path.add(name);
        }
        add(war, f, path);
      }
    }
    catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    //
    return war;
  }

  @Drone
  @Authentication
  WebDriver driver;

  private static void add(WebArchive war, File f, LinkedList<String> path) {
    if (f.isDirectory()) {
      File[] children = f.listFiles();
      if (children != null) {
        for (File child : children) {
          path.addLast(child.getName());
          add(war, child, path);
          path.removeLast();
        }
      }
    } else {
      war.addAsResource(f, Tools.join('/', path));
    }
  }

}
