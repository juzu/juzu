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

package inject.bound.provider.qualifier.declared;

import inject.AbstractInjectTestCase;
import inject.Color;
import inject.ColorizedLiteral;
import juzu.impl.inject.spi.InjectorProvider;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BoundProviderQualifierDeclaredTestCase<B, I> extends AbstractInjectTestCase<B, I> {

  public BoundProviderQualifierDeclaredTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Injected.class, null, null, null);
    Bean blue = new Bean();
    Bean red = new Bean();
    Bean green = new Bean.Green();
    bootstrap.bindProvider(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.BLUE)), new BeanProvider(blue));
    bootstrap.bindProvider(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.RED)), new BeanProvider(red));
    bootstrap.bindProvider(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.GREEN)), new BeanProvider(green));
    boot();

    //
    Injected injected = getBean(Injected.class);
    assertNotNull(injected);
    assertSame(blue, injected.blue);
    assertSame(red, injected.red);
    assertSame(green, injected.green);
  }
}
