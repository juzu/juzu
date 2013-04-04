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

package juzu.impl.router;

import junit.framework.Assert;
import juzu.test.AbstractTestCase;

import java.util.Arrays;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractControllerTestCase extends AbstractTestCase {
  public static void assertEquals(Map<String, String> expectedParameters, Map<String, String> parameters) {
    assertNotNull("Was not expecting a null parameter set", parameters);
    Assert.assertEquals(expectedParameters.keySet(), parameters.keySet());
    for (Map.Entry<String, String> expectedEntry : expectedParameters.entrySet()) {
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
