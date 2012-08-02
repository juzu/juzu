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

package juzu.test.protocol.portlet;

import juzu.impl.plugin.application.ApplicationRuntime;
import juzu.test.AbstractWebTestCase;
import juzu.test.protocol.mock.MockApplication;
import org.apache.pluto.container.driver.PlutoServices;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(Arquillian.class)
public abstract class AbstractPortletTestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    URL servletXML = AbstractPortletTestCase.class.getResource("web.xml");
    URL portletXML = AbstractPortletTestCase.class.getResource("portlet.xml");
    return ShrinkWrap.create(WebArchive.class, "juzu.war").
    addAsLibraries(DependencyResolvers.
      use(MavenDependencyResolver.class).
      loadEffectivePom("pom.xml")
      .artifacts("javax.servlet:jstl", "taglibs:standard").
      resolveAsFiles()).
    setWebXML(servletXML).
    addAsWebInfResource(portletXML, "portlet.xml");
  }

  @Override
  protected void doDeploy(MockApplication<?> application) {
    ApplicationRuntime.Provided.set(application.getRuntime());
  }

  @Override
  protected void doUndeploy(MockApplication<?> application) {
  }

  @AfterClass
  public static void killPlutoServices() {
    // We need to do this in order to fix a race condition between pluto services initialization by Spring
    // and arquillian extension when two test suite are executed in the same virtual machine.
    // Pluto services are modelled as a singleton and the org.jboss.portletbridge.test.CustomRenderServiceImpl
    // class look for the services before the test is initialized and find the previous services on a second
    // execution because the CustomRenderServiceImpl find the previous services.
    //
    try {
      Field field = PlutoServices.class.getDeclaredField("singleton");
      field.setAccessible(true);
      field.set(null, null);
    }
    catch (Exception e) {
      throw failure(e);
    }
  }
}
