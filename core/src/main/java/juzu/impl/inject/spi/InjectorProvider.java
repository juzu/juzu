/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.impl.inject.spi;

import juzu.impl.inject.spi.cdi.CDIBuilder;
import juzu.impl.inject.spi.guice.GuiceBuilder;
import juzu.impl.inject.spi.spring.SpringBuilder;

import javax.inject.Provider;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public enum InjectorProvider implements Provider<Injector> {

  CDI_WELD("weld") {
    public Injector get() {
      return new CDIBuilder();
    }
  },

  INJECT_GUICE("guice") {
    public Injector get() {
      return new GuiceBuilder();
    }
  },

  INJECT_SPRING("spring") {
    public Injector get() {
      return new SpringBuilder();
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
