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

package inject.resolvebeans;

import inject.AbstractInjectTestCase;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.common.Tools;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ResolvableBeansTestCase<B, I> extends AbstractInjectTestCase<B, I> {

  public ResolvableBeansTestCase(InjectorProvider di) {
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
