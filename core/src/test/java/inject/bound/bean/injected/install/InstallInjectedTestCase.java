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

package inject.bound.bean.injected.install;

import juzu.Scope;
import inject.AbstractInjectTestCase;
import juzu.impl.inject.spi.InjectorProvider;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class InstallInjectedTestCase<B, I> extends AbstractInjectTestCase<B, I> {

  public InstallInjectedTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();

    bootstrap.bindBean(Injected.class, null, new Injected());
    bootstrap.declareBean(Bean.class, Scope.SINGLETON, null, null);
    boot();

    //
    Injected injected = getBean(Injected.class);
    Bean bean = getBean(Bean.class);
    assertNotNull(injected.bean);
    assertSame(bean, injected.bean);
  }
}
