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
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** @author Julien Viet */
public abstract class AbstractExtendSpringTestCase extends AbstractWebTestCase {

  /** . */
  static final DescriptorBuilder CONFIG = DescriptorBuilder.DEFAULT.
      injector(InjectorProvider.INJECT_SPRING).
      listener("org.springframework.web.context.ContextLoaderListener");

  static WebArchive configure(WebArchive war) {
    war.addAsWebInfResource(new StringAsset(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" +
            "xmlns:context=\"http://www.springframework.org/schema/context\"\n" +
            "xmlns:p=\"http://www.springframework.org/schema/p\"\n" +
            "xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\n" +
            "http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-3.0.xsd \n" +
            "http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd\">\n" +
            "<bean name=\"foo\" scope=\"singleton\" class=\"bridge.inject.spring.SpringBean\"><constructor-arg index=\"0\" value=\"foo\"/></bean>" +
            "</beans>"), "applicationContext.xml");
    return war;
  }

  @Drone
  WebDriver driver;

  @Test
  public void testFoo() {
    driver.get(applicationURL().toString());
    WebElement spring = driver.findElement(By.id("spring"));
    assertNotNull(spring);
    assertEquals("foo", spring.getText());
  }
}
