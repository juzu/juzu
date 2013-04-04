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

package inject.configuration;

import inject.AbstractInjectTestCase;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.inject.spi.spring.SpringInjector;
import juzu.impl.common.Tools;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ConfigurationTestCase<B, I> extends AbstractInjectTestCase<B, I> {

  public ConfigurationTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testURL() throws Exception {
    if (di == InjectorProvider.INJECT_SPRING) {
      URL configurationURL = Bean.class.getResource("spring.xml");
      assertNotNull(configurationURL);
      InputStream in = configurationURL.openStream();
      assertNotNull(in);
      Tools.safeClose(in);

      //
      init();
      bootstrap.declareBean(Injected.class, null, null, null);
      ((SpringInjector)bootstrap).setConfigurationURL(configurationURL);
      boot();

      //
      Injected injected = getBean(Injected.class);
      assertNotNull(injected);
      assertNotNull(injected.getDeclared());

      //
      Bean declared = getBean(Bean.class);
      assertNotNull(declared);
      declared = (Bean)getBean("declared");
      assertNotNull(declared);
    }
  }
}
