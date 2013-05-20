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

package plugin.binding.scope;

import juzu.Response;
import juzu.View;

import javax.inject.Inject;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  /** . */
  private static int serial;

  @Inject
  Bean bean;

  @View
  public Response.Status index() throws IOException {
    if (bean != null) {
      serial = bean.getSerial();
      return Response.ok(A_.done().toString());
    }
    else {
      return Response.ok();
    }
  }

  @View
  public Response.Content done() throws IOException {
    if (bean != null) {
      if (serial + 1 == bean.getSerial()) {
        return Response.ok("pass");
      }
      else {
        return Response.ok("failure: was expecting to have" + serial + " + 1 == " + bean.getSerial());
      }
    }
    else {
      return Response.ok("failure");
    }
  }
}
