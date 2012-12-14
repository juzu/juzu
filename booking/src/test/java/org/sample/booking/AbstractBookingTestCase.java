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
