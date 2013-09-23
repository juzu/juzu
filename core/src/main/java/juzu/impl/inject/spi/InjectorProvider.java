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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public enum InjectorProvider {

  CDI_WELD("weld") {
    public Injector get() {
      try {
        Object manager = new InitialContext().lookup("java:comp/BeanManager");
        return ProvidedCDIInjector.get(manager);
      }
      catch (NamingException e) {
        // Not found
      }
      return new WeldInjector();
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

  public abstract Injector get();

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
    if ("guice".equals(value)) {
      return INJECT_GUICE;
    } else if ("spring".equals(value)) {
      return INJECT_SPRING;
    } else if ("weld".equals(value)) {
      return CDI_WELD;
    } else {
      return null;
    }
  }
}
