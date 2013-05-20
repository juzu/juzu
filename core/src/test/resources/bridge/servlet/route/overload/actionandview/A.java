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

package bridge.servlet.route.overload.actionandview;

import juzu.Action;
import juzu.Response;
import juzu.Route;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  /** . */
  static int count = 0;

  @juzu.View
  public Response.Content index() {
    count = 0;
    return Response.ok("" + A_.fooAction());
  }

  @Action
  @Route("/foo")
  public Response.View fooAction() {
    count = 1;
    return A_.fooView();
  }

  @juzu.View
  @Route("/foo")
  public Response.Content fooView() {
    return Response.ok(count == 1 ? "pass" : "fail");
  }
}
