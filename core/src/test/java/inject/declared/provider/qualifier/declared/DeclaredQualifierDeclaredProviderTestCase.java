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

package inject.declared.provider.qualifier.declared;

import inject.AbstractInjectTestCase;
import inject.Color;
import inject.ColorizedLiteral;
import juzu.impl.inject.spi.InjectorProvider;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DeclaredQualifierDeclaredProviderTestCase<B, I> extends AbstractInjectTestCase<B, I> {

  public DeclaredQualifierDeclaredProviderTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Injected.class, null, null, null);
    bootstrap.declareProvider(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.BLUE)), ColorlessProvider.class);
    bootstrap.declareProvider(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.RED)), ColorlessProvider.class);
    bootstrap.declareProvider(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.GREEN)), GreenProvider.class);
    boot();

    //
    Injected injected = getBean(Injected.class);
    assertNotNull(injected);
    assertNotNull(injected.blue);
    assertNotNull(injected.red);
    assertNotNull(injected.green);
    assertNotSame(injected.blue.getId(), injected.red.getId());
    assertNotSame(injected.green.getId(), injected.red.getId());
    assertNotSame(injected.blue.getId(), injected.green.getId());
    assertInstanceOf(Bean.class, injected.blue);
    assertInstanceOf(Bean.class, injected.red);
    assertInstanceOf(Bean.Green.class, injected.green);
    assertNotInstanceOf(Bean.Green.class, injected.blue);
    assertNotInstanceOf(Bean.Green.class, injected.red);
  }
}
