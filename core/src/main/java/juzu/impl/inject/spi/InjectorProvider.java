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

import juzu.PropertyType;
import juzu.impl.inject.spi.cdi.provided.ProvidedCDIInjector;
import juzu.impl.inject.spi.cdi.weld.WeldInjector;
import juzu.impl.inject.spi.guice.GuiceInjector;
import juzu.impl.inject.spi.spring.SpringInjector;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public enum InjectorProvider {

  CDI("cdi", 2) {
    private Object getManager() {
      try {
        // For EE
        return new InitialContext().lookup("java:comp/BeanManager");
      }
      catch (NamingException notFound1) {
        try {
          // For Tomcat
          return new InitialContext().lookup("java:comp/env/BeanManager");
        }
        catch (NamingException notFound2) {
          return null;
        }
      }
    }
    public Injector get() {
      return ProvidedCDIInjector.get(Thread.currentThread().getContextClassLoader());
    }
    @Override
    public boolean isAvailable() {
      return getManager() != null;
    }
  },

  WELD("weld", 3) {
    public Injector get() {
      return new WeldInjector();
    }
    @Override
    public boolean isAvailable() {
      try {
        Thread.currentThread().getContextClassLoader().loadClass("org.jboss.weld.bootstrap.WeldBootstrap");
        return true;
      }
      catch (Exception e) {
        return false;
      }
    }
  },

  GUICE("guice", 0) {
    public Injector get() {
      return new GuiceInjector();
    }

    @Override
    public boolean isAvailable() {
      try {
        Thread.currentThread().getContextClassLoader().loadClass("com.google.inject.Guice");
        return true;
      }
      catch (Exception e) {
        return false;
      }
    }
  },

  SPRING("spring", 1) {
    public Injector get() {
      return new SpringInjector();
    }
    @Override
    public boolean isAvailable() {
      try {
        Thread.currentThread().getContextClassLoader().loadClass("org.springframework.beans.factory.support.DefaultListableBeanFactory");
        return true;
      }
      catch (Exception e) {
        return false;
      }
    }
  };

  /** The property. */
  public static PropertyType<InjectorProvider> PROPERTY = new PropertyType<InjectorProvider>(){};

  public abstract Injector get();

  /** . */
  final String value;

  /** . */
  final int priority;

  private InjectorProvider(String value, int priority) {
    this.value = value;
    this.priority = priority;
  }

  public String getValue() {
    return value;
  }

  /**
   * @return the priority of the injector when there is an ambiguity when chosing the injector
   */
  public int getPriority() {
    return priority;
  }

  /**
   * @return true when the provider is available.
   */
  public abstract boolean isAvailable();

  public static InjectorProvider find(String value) {
    if ("guice".equals(value)) {
      return GUICE;
    } else if ("spring".equals(value)) {
      return SPRING;
    } else if ("weld".equals(value)) {
      return WELD;
    } else if ("cdi".equals(value)) {
      return CDI;
    } else {
      return null;
    }
  }
}
