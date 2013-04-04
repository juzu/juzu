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

package inject.bound.provider.scope.declared;

import juzu.Scope;
import inject.AbstractInjectTestCase;
import juzu.impl.inject.spi.InjectorProvider;
import inject.ScopedKey;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BoundProviderScopeDeclaredTestCase<B, I> extends AbstractInjectTestCase<B, I> {

  public BoundProviderScopeDeclaredTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    BeanProvider provider = new BeanProvider();

    //
    init();
    bootstrap.declareBean(Injected.class, null, null, null);
    bootstrap.bindProvider(Bean.class, Scope.REQUEST, null, provider);
    boot(Scope.REQUEST);

    //
    beginScoping();
    try {
      assertEquals(0, scopingContext.getEntries().size());
      Injected injected = getBean(Injected.class);
      assertNotNull(injected);
      assertNotNull(injected.injected);
      String value = injected.injected.getValue();
      assertEquals(1, scopingContext.getEntries().size());
      ScopedKey key = scopingContext.getEntries().keySet().iterator().next();
      assertEquals(Scope.REQUEST, key.getScope());
      Bean scoped = (Bean)scopingContext.getEntries().get(key).get();
      assertEquals(scoped.getValue(), value);
      assertSame(scoped, provider.bean);
    }
    finally {
      endScoping();
    }
  }
}
