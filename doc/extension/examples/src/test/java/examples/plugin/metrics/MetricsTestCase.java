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
package examples.plugin.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.test.AbstractTestCase;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockViewBridge;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

/**
 * @author Julien Viet
 */
public class MetricsTestCase extends AbstractTestCase {

  // tag::testTimer[]
  @Test
  public void testTimer() throws Exception {
    SharedMetricRegistries.clear();                                                             //<1>
    MockApplication<File> application = application(InjectorProvider.GUICE, "examples.metrics");//<2>
    application.init();
    MockClient client = application.client();
    MockViewBridge view = client.render();                                                      //<3>
    assertEquals("ok", view.assertStringResponse());
    assertEquals(Collections.singleton("examples.metrics"), SharedMetricRegistries.names());    //<4>
    MetricRegistry registry = SharedMetricRegistries.getOrCreate("examples.metrics");
    Timer timer = registry.getTimers().get("juzu.responses");                                   //<5>
    assertEquals(1, timer.getCount());
    Meter meter = registry.getMeters().get("custom");                                           //<6>
    assertNotNull(meter);
  }
  // end::testTimer[]
}
