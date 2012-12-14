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

package examples.tutorial;

import junit.framework.AssertionFailedError;
import juzu.arquillian.Helper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class WeatherServletTestCase extends WeatherTestCase {

  @Deployment
  public static WebArchive deployment() {
    WebArchive war = Helper.createBaseServletDeployment(); // <1> Create the base servlet deployment
    war.addAsWebInfResource(new File("src/test/resources/spring.xml")); // <2> Add the spring.xml descriptor
    war.addPackages(true, "examples.tutorial"); // <3> Add the examples.tutorial package
    return war;
  }

  public URL getApplicationURL(String application) {
    try {
      return deploymentURL.toURI().resolve(application).toURL();
    }
    catch (Exception e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }
}
