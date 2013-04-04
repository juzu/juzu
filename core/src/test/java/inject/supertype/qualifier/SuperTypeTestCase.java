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

package inject.supertype.qualifier;

import inject.AbstractInjectTestCase;
import juzu.impl.inject.spi.InjectorProvider;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SuperTypeTestCase<B, I> extends AbstractInjectTestCase<B, I> {

  public SuperTypeTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testSuperType() throws Exception {
    init();
    bootstrap.declareBean(Fruit.class, null, null, Apple.class);
    bootstrap.declareBean(InjectedWithSuperType.class, null, null, null);
    bootstrap.declareBean(InjectedWithActualType.class, null, null, null);
    boot();

    //
    InjectedWithSuperType withSuperType = getBean(InjectedWithSuperType.class);
    assertNotNull(withSuperType);
    assertNotNull(withSuperType.fruit);

    //
    InjectedWithActualType withActualType = getBean(InjectedWithActualType.class);
    assertNotNull(withActualType);
    assertNotNull(withActualType.apple);
  }
}
