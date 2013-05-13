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

package inject.copy;

import inject.AbstractInjectTestCase;
import juzu.Scope;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.inject.spi.Injector;
import juzu.impl.inject.spi.InjectorProvider;
import org.junit.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Properties;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CopyTestCase<B, I> extends AbstractInjectTestCase<B, I> {

  public CopyTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    HashSet singleton = new HashSet();
    bootstrap.declareBean(Properties.class, Scope.SINGLETON, null, null);
    bootstrap.bindBean(HashSet.class, null, singleton);
    Injector injector = bootstrap.copy();
    bootstrap.declareBean(Date.class, Scope.SINGLETON, null, null);
    boot();
    InjectionContext context = boot(injector);

    //
    Properties p1 = getBean(Properties.class);
    Properties p2 = getBean(Properties.class);
    assertSame(p1, p2);
    Date s1 = getBean(Date.class);
    Date s2 = getBean(Date.class);
    assertSame(s1, s2);
    assertSame(singleton, getBean(HashSet.class));

    //
    Properties p3 = getBean(context, Properties.class);
    Properties p4 = getBean(context, Properties.class);
    assertSame(p3, p4);
    assertNotSame(p1, p3);
    if (di != InjectorProvider.INJECT_GUICE) {
      assertNull(context.get(Date.class));
    }
    assertSame(singleton, getBean(context, HashSet.class));
  }
}
