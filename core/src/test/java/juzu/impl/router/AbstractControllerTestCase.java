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

package juzu.impl.router;

import junit.framework.Assert;
import juzu.impl.common.QualifiedName;
import juzu.test.AbstractTestCase;

import java.util.Arrays;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractControllerTestCase extends AbstractTestCase {
  public static void assertEquals(Map<QualifiedName, String> expectedParameters, Map<QualifiedName, String> parameters) {
    assertNotNull("Was not expecting a null parameter set", parameters);
    Assert.assertEquals(expectedParameters.keySet(), parameters.keySet());
    for (Map.Entry<QualifiedName, String> expectedEntry : expectedParameters.entrySet()) {
      Assert.assertEquals(expectedEntry.getValue(), parameters.get(expectedEntry.getKey()));
    }
  }

  public static void assertMapEquals(Map<String, String[]> expectedParameters, Map<String, String[]> parameters) {
    assertNotNull("Was not expecting a null parameter set", parameters);
    Assert.assertEquals(expectedParameters.keySet(), parameters.keySet());
    for (Map.Entry<String, String[]> expectedEntry : expectedParameters.entrySet()) {
      Assert.assertEquals(Arrays.asList(expectedEntry.getValue()), Arrays.asList(parameters.get(expectedEntry.getKey())));
    }
  }
}
