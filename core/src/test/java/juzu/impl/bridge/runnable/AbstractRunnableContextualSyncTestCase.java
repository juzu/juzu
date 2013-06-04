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
package juzu.impl.bridge.runnable;

import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

/** @author Julien Viet */
public abstract class AbstractRunnableContextualSyncTestCase extends AbstractWebTestCase {

  @Drone
  WebDriver driver;

  /** . */
  public static String requestURL;

  /** . */
  public static String runnableURL;

  /** . */
  public static Object requestObject;

  /** . */
  public static Object runnableObject;

  /** . */
  public static boolean runnableActive;

  @Test
  public void testPathParam() throws Exception {
    driver.get(applicationURL().toString());
    assertTrue(driver.getPageSource().contains("pass"));
    assertEquals(requestURL, runnableURL);
    assertSame(requestObject, runnableObject);
    assertTrue(runnableActive);
  }
}
