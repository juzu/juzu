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

package bridge.runmode.live;

import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.impl.bridge.runmode.AbstractRunModeLiveTestCase;
import juzu.impl.common.RunMode;
import juzu.impl.request.Request;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {


  /** . */
  private static int count = 0;

  @View
  @Route("/foo")
  public Response.Content index() {
    ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
    ClassLoader aCL = getClass().getClassLoader();
    ClassLoader bCL = B.class.getClassLoader();
    AbstractRunModeLiveTestCase.SAME_CL_1 = aCL == threadCL;
    AbstractRunModeLiveTestCase.SAME_CL_2 = bCL == threadCL;
    AbstractRunModeLiveTestCase.RUN_MODE = Request.getCurrent().getBridge().getProperty(RunMode.PROPERTY);
    if (count == 0) {
      count = 1;
      String url = A_.index().toString();
      return Response.ok("<a id='trigger' href='" + url + "'>click</div>");
    } else if (count == 1) {
      count = 2;
      return Response.ok("ok");
    } else {
      throw new RuntimeException("throwed");
    }
  }
}
