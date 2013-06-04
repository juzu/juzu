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

import java.util.concurrent.atomic.AtomicBoolean;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractRunnableAsyncTestCase extends AbstractWebTestCase {

  @Drone
  WebDriver driver;

  /** . */
  public static String requestURL;

  /** . */
  public static String runnableURL;

  /** . */
  public static final AtomicBoolean destroyed = new AtomicBoolean();

  /** . */
  public static boolean requestDestroyed;

  /** . */
  public static boolean runnableDestroyed;

  /** . */
  public static boolean runnableActive;

  @Test
  public void testPathParam() throws Exception {
    destroyed.set(true);
    driver.get(applicationURL().toString());
    assertTrue(driver.getPageSource().contains("pass"));
    assertNotSame(requestURL, runnableURL);
    assertEquals("null", runnableURL);
    assertFalse(requestDestroyed);
    assertFalse(runnableActive);

    // This is a bit random-ish as we assume that the 500mn are enough for destroying the
    // controller bean
    assertTrue(runnableDestroyed);
    assertTrue(destroyed.get());
  }
}
