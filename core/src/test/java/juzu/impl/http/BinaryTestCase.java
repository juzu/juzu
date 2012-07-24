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

package juzu.impl.http;

import juzu.impl.common.Tools;
import juzu.test.protocol.http.AbstractHttpTestCase;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BinaryTestCase extends AbstractHttpTestCase {

  @Drone
  WebDriver driver;

  @Test
  public void testBinary() throws Exception {
    assertDeploy("http", "binary");
    driver.get(deploymentURL.toString());
    String url = driver.findElement(By.tagName("body")).getText();
    URLConnection conn = new URL(url).openConnection();
    conn.connect();
    InputStream in = conn.getInputStream();
    String s = Tools.read(in);
    assertEquals("hello", s);
    assertEquals("application/octet-stream", conn.getContentType());
  }
}
