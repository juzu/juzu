/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package juzu.test.protocol.standalone;

import juzu.impl.application.ApplicationRuntime;
import juzu.impl.inject.spi.InjectImplementation;
import juzu.test.AbstractWebTestCase;
import juzu.test.protocol.mock.MockApplication;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.net.URL;
import java.util.Arrays;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractStandaloneTestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    URL descriptor = AbstractStandaloneTestCase.class.getResource("web.xml");
/*
    URL jquery = HttpServletImpl.class.getResource("jquery-1.7.1.js");
    URL test = HttpServletImpl.class.getResource("test.js");
    URL stylesheet = HttpServletImpl.class.getResource("main.css");
*/
    return ShrinkWrap.create(WebArchive.class, "juzu.war").
//      addAsWebResource(jquery, "jquery.js").
//      addAsWebResource(test, "test.js").
//      addAsWebResource(stylesheet, "main.css").
      setWebXML(descriptor);
  }

  /** The currently deployed application. */
  private MockApplication<?> application;

  @Override
  public MockApplication<?> assertDeploy(String... packageName) {
    try {
      application = application(InjectImplementation.CDI_WELD, packageName);
      ApplicationRuntime.Provided.set(application.getRuntime());
      return application;
    }
    catch (Exception e) {
      throw failure("Could not deploy application " + Arrays.asList(packageName), e);
    }
  }
}
