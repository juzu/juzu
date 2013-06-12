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

import juzu.impl.inject.spi.cdi.provided.ProvidedCDIInjector;
import juzu.impl.inject.spi.cdi.weld.WeldInjector;
import juzu.impl.inject.spi.guice.GuiceInjector;
import juzu.impl.inject.spi.spring.SpringInjector;

import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public enum InjectorProvider {

  CDI_WELD("weld") {
    public Injector get(boolean provided) {
      if (provided) {
        ClassLoader key = Thread.currentThread().getContextClassLoader();
        return ProvidedCDIInjector.get(key);
      } else {
        return new WeldInjector();
      }
    }
  },

  INJECT_GUICE("guice") {
    public Injector get(boolean provided) {
      if (provided) {
        throw new UnsupportedOperationException("No provided mode for Guice");
      } else {
        return new GuiceInjector();
      }
    }
  },

  INJECT_SPRING("spring") {
    public Injector get(boolean provided) {
      if (provided) {
        throw new UnsupportedOperationException("No provided mode for Spring");
      } else {
        return new SpringInjector();
      }
    }
  };

  public abstract Injector get(boolean provided);

  /** . */
  final String value;

  private InjectorProvider(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  /** . */
  private static final Map<String, InjectorProvider> LOOKUP = new HashMap<String, InjectorProvider>();

  static {
    for (InjectorProvider injectorProvider : values()) {
      LOOKUP.put(injectorProvider.getValue(), injectorProvider);
    }
  }

  public static InjectorProvider find(String value) {
    return LOOKUP.get(value);
  }
}
