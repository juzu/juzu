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

package juzu.impl.plugin.controller;

import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.value.ValueType;
import juzu.processor.MainProcessor;
import juzu.test.AbstractTestCase;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockViewBridge;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.text.ParseException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ValueTypeTestCase extends AbstractTestCase {

  /** . */
  private File providers;

  public ValueTypeTestCase() {
  }

  @Override
  public void setUp() throws Exception {
  }

  @Override
  public void tearDown() {
    if (providers != null) {
      // Cleanup if needed
      assertTrue(providers.delete());
    }
  }

  private void deploy(String valueType) throws Exception {
    URL a = MainProcessor.class.getClassLoader().getResource(ValueTypeTestCase.class.getName().replace('.', '/') + ".class");
    File classes = new File(a.toURI()).getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
    File metaInf = new File(classes, "META-INF");
    File services = new File(metaInf, "services");
    services.mkdirs();
    assertTrue(services.exists());
    providers = new File(services, ValueType.class.getName());
    new FileWriter(providers).append(valueType).close();
  }

  @Test
  public void testCustom() throws Exception {
    deploy("plugin.controller.valuetype.custom.FooType");
    runSuccess("plugin.controller.valuetype.custom");
  }

  @Test
  public void testInteger() throws Exception {
    runSuccess("plugin.controller.valuetype.integer");
  }

  @Test
  public void testPrimitiveInteger() throws Exception {
    runSuccess("plugin.controller.valuetype.primitive.integer");
  }

  @Test
  public void testPrimitiveArray() throws Exception {
    runSuccess("plugin.controller.valuetype.primitive.array");
  }

  @Test
  public void testDate() throws Exception {
    runSuccess("plugin.controller.valuetype.date");
  }

  @Test
  public void testBean() throws Exception {
    runSuccess("plugin.controller.valuetype.bean");
  }

  @Test
  public void testParseError() throws Exception {
    deploy("plugin.controller.valuetype.error.parse.LocaleType");
    ParseException error = runParseError("plugin.controller.valuetype.error.parse", ParseException.class);
    assertEquals(0, error.getErrorOffset());
    assertEndsWith("Normal behavior", error.getMessage());
  }

  private void runSuccess(String pkg) throws Exception {
    MockApplication<File> application = application(InjectorProvider.GUICE, pkg);
    MockApplication<?> app = application.init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    MockViewBridge action = (MockViewBridge)client.invoke(render.assertStringResult());
    String result = action.assertStringResult();
    assertEquals("pass", result);
  }

  private <T extends Throwable> T  runParseError(String pkg, Class<T> expected) throws Exception {
    MockApplication<File> application = application(InjectorProvider.GUICE, pkg);
    MockApplication<?> app = application.init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    MockViewBridge action = (MockViewBridge)client.invoke(render.assertStringResult());
    return action.assertFailure(expected);
  }
}
