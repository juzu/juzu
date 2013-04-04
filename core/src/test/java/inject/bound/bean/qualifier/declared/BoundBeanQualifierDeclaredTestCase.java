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

package inject.bound.bean.qualifier.declared;

import inject.AbstractInjectTestCase;
import inject.Color;
import inject.ColorizedLiteral;
import juzu.impl.inject.spi.InjectorProvider;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BoundBeanQualifierDeclaredTestCase<B, I> extends AbstractInjectTestCase<B, I> {

  public BoundBeanQualifierDeclaredTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    Bean blue = new Bean();
    Bean red = new Bean();
    init();
    bootstrap.declareBean(Injected.class, null, null, null);
    bootstrap.bindBean(Bean.class, Collections.<Annotation>singleton(new ColorizedLiteral(Color.BLUE)), blue);
    bootstrap.bindBean(Bean.class, Collections.<Annotation>singleton(new ColorizedLiteral(Color.RED)), red);
    boot();

    //
    Injected injected = getBean(Injected.class);
    assertNotNull(injected);
    assertNotNull(injected.blue);
    assertNotNull(injected.red);
    assertSame(blue, injected.blue);
    assertSame(red, injected.red);
  }
}

