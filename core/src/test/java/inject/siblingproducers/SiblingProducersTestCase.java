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

package inject.siblingproducers;

import inject.AbstractInjectTestCase;
import juzu.impl.inject.spi.InjectorProvider;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SiblingProducersTestCase<B, I> extends AbstractInjectTestCase<B, I> {

  public SiblingProducersTestCase(InjectorProvider di) {
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
