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

package juzu.test;

import juzu.impl.inject.spi.InjectImplementation;
import juzu.test.protocol.mock.MockApplication;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(value = Parameterized.class)
public abstract class AbstractInjectTestCase extends AbstractTestCase {

  /** . */
  protected final InjectImplementation di;

  protected AbstractInjectTestCase(InjectImplementation di) {
    this.di = di;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    Object[][] data = new Object[][]{
      {InjectImplementation.CDI_WELD},
      {InjectImplementation.INJECT_SPRING},
      {InjectImplementation.INJECT_GUICE}
    };
    return Arrays.asList(data);
  }

  public InjectImplementation getDI() {
    return di;
  }

  public MockApplication<?> application(String... packageName) {
    return application(di, packageName);
  }
}
