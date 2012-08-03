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

package juzu.test.protocol.standalone;

import juzu.impl.plugin.application.ApplicationRuntime;
import juzu.test.AbstractWebTestCase;
import juzu.test.protocol.mock.MockApplication;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractStandaloneTestCase extends AbstractWebTestCase {

  static ClassLoader loader;

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

  @Override
  protected void doDeploy(MockApplication<?> application) {
    loader = application.getRuntime().getClassLoader();
    ApplicationRuntime.Provided.set(application.getRuntime());
  }

  @Override
  protected void doUndeploy(MockApplication<?> application) {
  }
}
