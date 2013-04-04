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
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.sample.booking.qualifier.Authentication;

import java.io.File;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(Arquillian.class)
public abstract class AbstractBookingTestCase {

  public static WebArchive createDeployment() {
    WebArchive war = Helper.createBasePortletDeployment();
    war.addAsWebInfResource(new File("src/main/webapp/WEB-INF/portlet.xml"));
    war.addAsWebResource(new File("src/main/webapp/public/javascripts/jquery-1.7.1.min.js"), "public/javascripts/jquery-1.7.1.min.js");
    war.addAsWebResource(new File("src/main/webapp/public/javascripts/jquery-ui-1.7.2.custom.min.js"), "public/javascripts/jquery-ui-1.7.2.custom.min.js");
    war.addAsWebResource(new File("src/main/webapp/public/javascripts/booking.js"), "public/javascripts/booking.js");
    war.addAsWebResource(new File("src/main/webapp/public/stylesheets/main.css"), "public/stylesheets/main.css");
    war.addAsWebResource(new File("src/main/webapp/public/ui-lightness/jquery-ui-1.7.2.custom.css"), "public/ui-lightness/jquery-ui-1.7.2.custom.css");
    war.addPackages(true, Flash.class.getPackage());
    return war;
  }

  @Drone
  @Authentication
  WebDriver driver;

}
