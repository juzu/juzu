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

package examples.tutorial;

import junit.framework.AssertionFailedError;
import juzu.arquillian.Helper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class WeatherPortletTestCase extends WeatherTestCase {

  @Deployment
  public static WebArchive deployment() {
    WebArchive war = Helper.createBasePortletDeployment("spring");
    war.addAsWebInfResource(new File("src/main/webapp/WEB-INF/portlet.xml"));
    war.addAsWebInfResource(new File("src/test/resources/spring.xml"));
    war.addPackages(true, "examples.tutorial");
    return war;
  }

  @Override
  public URL getApplicationURL(String application) {
    try {
      return deploymentURL.toURI().resolve("embed/" + application).toURL();
    }
    catch (Exception e) {
      AssertionFailedError afe = new AssertionFailedError("Could not build URL");
      afe.initCause(e);
      throw afe;
    }
  }
}
