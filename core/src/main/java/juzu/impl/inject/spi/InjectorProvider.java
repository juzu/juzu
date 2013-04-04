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

package juzu.impl.inject.spi;

import juzu.impl.inject.spi.cdi.CDIInjector;
import juzu.impl.inject.spi.guice.GuiceInjector;
import juzu.impl.inject.spi.spring.SpringInjector;

import javax.inject.Provider;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public enum InjectorProvider implements Provider<Injector> {

  CDI_WELD("weld") {
    public Injector get() {
      return new CDIInjector();
    }
  },

  INJECT_GUICE("guice") {
    public Injector get() {
      return new GuiceInjector();
    }
  },

  INJECT_SPRING("spring") {
    public Injector get() {
      return new SpringInjector();
    }
  };

  /** . */
  final String value;

  private InjectorProvider(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
