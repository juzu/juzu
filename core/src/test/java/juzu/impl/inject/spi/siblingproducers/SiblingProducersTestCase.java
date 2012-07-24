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

package juzu.impl.inject.spi.siblingproducers;

import juzu.impl.inject.spi.AbstractInjectManagerTestCase;
import juzu.impl.inject.spi.InjectImplementation;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SiblingProducersTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public SiblingProducersTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Injected.class, null, null, null);
    bootstrap.declareProvider(Bean1.class, null, null, Bean1Provider.class);
    bootstrap.declareProvider(Bean2.class, null, null, Bean2Provider.class);
    bootstrap.addFileSystem(fs);
    boot();

    //
    Bean1 productExt1 = getBean(Bean1.class);
    assertNotNull(productExt1);

    //
    Bean2 productExt2 = getBean(Bean2.class);
    assertNotNull(productExt2);

    //
    Injected productInjected = getBean(Injected.class);
    assertNotNull(productInjected);
    assertNotNull(productInjected.productExt1);
    assertNotNull(productInjected.productExt2);
  }
}
