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

package juzu.impl.inject.spi.qualifier;

import juzu.impl.inject.spi.AbstractInjectManagerTestCase;
import juzu.impl.inject.spi.InjectImplementation;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class QualifierTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public QualifierTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Injected.class, null, null, (Class<Injected>)null);
    bootstrap.declareBean(Bean.class, null, null, Bean.Red.class);
    bootstrap.declareBean(Bean.class, null, null, Bean.Green.class);
    boot();

    //
    Injected beanObject = getBean(Injected.class);
    assertNotNull(beanObject);
    assertNotNull(beanObject.getRed());
    assertEquals(Bean.Red.class, beanObject.getRed().getClass());
    assertNotNull(beanObject.getGreen());
    assertEquals(Bean.Green.class, beanObject.getGreen().getClass());
  }
}
