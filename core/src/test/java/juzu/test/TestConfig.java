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
package juzu.test;

import juzu.impl.common.RunMode;
import juzu.impl.inject.spi.InjectorProvider;

/** @author Julien Viet */
public class TestConfig {

  /** . */
  private RunMode runMode = RunMode.PROD;

  /** . */
  private InjectorProvider injector = InjectorProvider.INJECT_GUICE;

  /** . */
  private String urlPattern = "/";

  public TestConfig runMode(RunMode runMode) {
    this.runMode = runMode;
    return this;
  }

  public TestConfig injector(InjectorProvider injector) {
    this.injector = injector;
    return this;
  }

  public TestConfig urlPattern(String urlPattern) {
    this.urlPattern = urlPattern;
    return this;
  }

  public RunMode getRunMode() {
    return runMode;
  }

  public InjectorProvider getInjector() {
    return injector;
  }

  public String getURLPattern() {
    return urlPattern;
  }
}
