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

package juzu.impl.inject.spi.resolvebeans;

import juzu.impl.inject.spi.AbstractInjectManagerTestCase;
import juzu.impl.inject.spi.InjectImplementation;
import juzu.impl.common.Tools;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ResolvableBeansTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public ResolvableBeansTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Bean1.class, null, null, null);
    bootstrap.declareBean(Bean2.class, null, null, null);
    boot();

    //
    ArrayList<B> beans = Tools.list(mgr.resolveBeans(AbstractBean.class));
    assertEquals(2, beans.size());
    HashSet<Class<?>> classes = new HashSet<Class<?>>();
    for (B bean : beans) {
      I instance = mgr.create(bean);
      Object o = mgr.get(bean, instance);
      classes.add(o.getClass());
    }
    assertEquals(Tools.<Class<?>>set(Bean1.class, Bean2.class), classes);
  }
}
