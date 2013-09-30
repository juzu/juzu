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

package inject.named.bean;

import inject.AbstractInjectTestCase;
import juzu.impl.inject.spi.InjectorProvider;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class NamedTestCase<B, I> extends AbstractInjectTestCase<B, I> {

  public NamedTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Injected.class, null, null, null);
    bootstrap.declareBean(Bean.class, null, null, Bean.Foo.class);
    bootstrap.declareBean(Bean.class, null, null, Bean.Bar.class);
    boot();

    //
    Injected beanObject = getBean(Injected.class);
    assertNotNull(beanObject);
    assertNotNull(beanObject.getFoo());
    assertEquals(Bean.Foo.class, beanObject.getFoo().getClass());
    assertNotNull(beanObject.getBar());
    assertEquals(Bean.Bar.class, beanObject.getBar().getClass());

    //
    Object foo = getBean("foo");
    assertNotNull(foo);

    //
    assertNull(mgr.resolveBean("juu"));
  }
}
