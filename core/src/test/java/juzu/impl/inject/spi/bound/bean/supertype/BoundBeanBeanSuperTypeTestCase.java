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

package juzu.impl.inject.spi.bound.bean.supertype;

import juzu.impl.inject.spi.AbstractInjectManagerTestCase;
import juzu.impl.inject.spi.InjectImplementation;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BoundBeanBeanSuperTypeTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public BoundBeanBeanSuperTypeTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    Apple apple = new Apple();
    bootstrap.bindBean(Fruit.class, null, apple);
    bootstrap.declareBean(Injected.class, null, null, null);
    boot();

    //
    Injected beanObject = getBean(Injected.class);
    assertNotNull(beanObject);
    assertNotNull(beanObject.fruit);
    assertSame(apple, beanObject.fruit);
  }
}
