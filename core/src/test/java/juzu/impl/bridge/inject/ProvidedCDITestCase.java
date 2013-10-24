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
package juzu.impl.bridge.inject;

import juzu.impl.bridge.DescriptorBuilder;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

/** @author Julien Viet */
public class ProvidedCDITestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    DescriptorBuilder config = DescriptorBuilder.DEFAULT.
        injector(InjectorProvider.CDI).
        servletApp("bridge.inject.providedcdi").
        listener("org.jboss.weld.environment.servlet.Listener").embedPortletContainer().
        resourceEnvRef("BeanManager", "javax.enterprise.inject.spi.BeanManager");
    WebArchive war = createServletDeployment(config, true);
    war.addAsManifestResource(new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<Context>\n" +
        "<Resource name=\"BeanManager\" auth=\"Container\" type=\"javax.enterprise.inject.spi.BeanManager\" factory=\"org.jboss.weld.resources.ManagerObjectFactory\"/>\n" +
        "<WatchedResource>WEB-INF/web.xml</WatchedResource>\n" +
        "<WatchedResource>META-INF/context.xml</WatchedResource>\n" +
        "</Context>"), "context.xml");
    war.addAsWebInfResource(new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<beans xmlns=\"http://java.sun.com/xml/ns/javaee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/beans_1_0.xsd\">\n" +
        "</beans>"), "beans.xml");
    return war;
  }

  @Drone
  WebDriver driver;

  @Test
  public void testFoo() {
    driver.get(applicationURL().toString());
    String page = driver.getPageSource();
    assertTrue("Was expecting to find 'pass' in " + page, page.contains("pass"));
  }
}
