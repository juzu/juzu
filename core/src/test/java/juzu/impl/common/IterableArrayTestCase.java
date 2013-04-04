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

package juzu.impl.common;

import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class IterableArrayTestCase extends AbstractTestCase {

  @Test
  public void testSimple() {
    String[] a = {"a", "b"};
    assertEquals(Collections.<String>emptyList(), Tools.list(new IterableArray<String>(a, 0, 0)));
    assertEquals(Arrays.asList("a"), Tools.list(new IterableArray<String>(a, 0, 1)));
    assertEquals(Arrays.asList("a", "b"), Tools.list(new IterableArray<String>(a, 0, 2)));
    assertEquals(Arrays.asList("b"), Tools.list(new IterableArray<String>(a, 1, 2)));
  }
}
