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

import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DuplicateParamTestCase extends AbstractControllerTestCase {

  @Test
  public void testPathParamDuplicatesPathParam() throws Exception {
    try {
      new Router().
          append("/{foo}").
          append("/{foo}");
      fail();
    }
    catch (MalformedRouteException e) {
    }

    //
    try {
      new Router().
          append("/{foo}").
          append("/bar").append("/{foo}");
      fail();
    }
    catch (MalformedRouteException e) {
    }
  }

}
