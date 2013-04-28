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

package juzu.plugin.upload;

import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractUploadTestCase extends AbstractWebTestCase {

  /** . */
  public static String contentType;

  /** . */
  public static String content;

  /** . */
  public static String text;

  /** . */
  public static String field;

  @Drone
  WebDriver driver;

  protected abstract URL getURL();

  @Test
  public void testUpload() throws Exception {
    driver.get(getURL().toString());
    WebElement submit = driver.findElement(By.id("submit"));
    WebElement file = driver.findElement(By.id("file"));
    WebElement text = driver.findElement(By.id("text"));
    WebElement field = driver.findElement(By.id("field"));
    File f = File.createTempFile("juzu", ".txt");
    f.deleteOnExit();
    FileWriter writer = new FileWriter(f);
    writer.write("HELLO");
    writer.close();
    file.sendKeys(f.getAbsolutePath());
    text.sendKeys("text_value");
    field.sendKeys("field_value");
    AbstractUploadTestCase.contentType = null;
    AbstractUploadTestCase.content = null;
    AbstractUploadTestCase.text = null;
    submit.submit();
    assertEquals("text/plain", AbstractUploadTestCase.contentType);
    assertEquals("HELLO", AbstractUploadTestCase.content);
    assertEquals("text_value", AbstractUploadTestCase.text);
    assertEquals("field_value", AbstractUploadTestCase.field);
  }
}
