/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.impl.inject.spi.declared.provider.qualifier.declared;

import juzu.impl.inject.spi.AbstractInjectManagerTestCase;
import juzu.impl.inject.spi.Color;
import juzu.impl.inject.spi.ColorizedLiteral;
import juzu.impl.inject.spi.InjectImplementation;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DeclaredQualifierDeclaredProviderTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public DeclaredQualifierDeclaredProviderTestCase(InjectImplementation di) {
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
