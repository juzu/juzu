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

package juzu.impl.bridge.servlet;

import junit.framework.Assert;
import juzu.test.protocol.standalone.AbstractStandaloneTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ClientContextTestCase extends AbstractStandaloneTestCase {

  /** . */
  public static String kind;

  /** . */
  public static String contentType;

  /** . */
  public static String charset;

  /** . */
  public static String content;

  /** . */
  public static int contentLength;

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createDeployment("bridge.client.action");
  }

  @Drone
  WebDriver driver;

  @Test
  public void testAction() throws Exception {
    test("action");
  }

  @Test
  public void testResource() throws Exception {
    test("resource");
  }

  private void test(String kind) throws Exception {
    driver.get(deploymentURL.toString());
    WebElement link = driver.findElement(By.id(kind));
    contentLength = -1;
    charset = null;
    contentType = null;
    content = null;
    URL url = new URL(link.getAttribute("href"));
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setDoInput(true);
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-type", "application/octet-stream;charset=UTF8");
    conn.connect();
    OutputStream out = conn.getOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
    writer.write("foo");
    writer.flush();
    Assert.assertEquals(200, conn.getResponseCode());
    assertEquals(3, contentLength);
    assertEquals("UTF8", charset);
    assertEquals("application/octet-stream;charset=UTF8", contentType);
    assertEquals("foo", content);
    assertEquals(kind, ClientContextTestCase.kind);
  }
}
