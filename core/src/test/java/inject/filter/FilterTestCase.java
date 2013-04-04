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

package inject.filter;

import inject.AbstractInjectTestCase;
import juzu.impl.common.Filter;
import juzu.impl.inject.spi.InjectorProvider;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class FilterTestCase<B, I> extends AbstractInjectTestCase<B, I> {

  public FilterTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Bean.class, null, null, Bean1.class);
    bootstrap.declareBean(Injected.class, null, null, null);
    boot(new Filter<Class<?>>() {
      public boolean accept(Class<?> elt) {
        return !elt.equals(Bean2.class);
      }
    });

    //
    Injected injected = getBean(Injected.class);
    assertNotNull(injected);
    assertInstanceOf(Bean1.class, injected.bean);
  }
}
