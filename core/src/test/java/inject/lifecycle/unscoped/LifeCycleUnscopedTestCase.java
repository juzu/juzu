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

package inject.lifecycle.unscoped;

import inject.AbstractInjectTestCase;
import juzu.impl.inject.spi.InjectImplementation;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LifeCycleUnscopedTestCase<B, I> extends AbstractInjectTestCase<B, I> {

  public LifeCycleUnscopedTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Bean.class, null, null, null);
    boot();

    //
    Bean.construct = 0;
    Bean.destroy = 0;

    //
    beginScoping();
    try {
      B bean = mgr.resolveBean(Bean.class);
      I instance = mgr.create(bean);
      Bean o = (Bean)mgr.get(bean, instance);
      assertEquals(1, Bean.construct);
      assertEquals(0, Bean.destroy);
      mgr.release(bean, instance);
      assertEquals(1, Bean.construct);
      assertEquals(1, Bean.destroy);
    }
    finally {
      endScoping();
    }
    assertEquals(1, Bean.construct);
    assertEquals(1, Bean.destroy);
  }
}
