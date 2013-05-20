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

package actionpathparam;

import juzu.Action;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.bridge.vertx.ActionPathParamTestCase;

public class A {

  @View
  public Response.Status index() {
    ActionPathParamTestCase.action = "" + A_.action("foo");
    return Response.ok();
  }

  @Action
  @Route("/action/{param}")
  public Response.View action(String param) {
    return A_.view("bar" + param);
  }

  @View
  @Route("/view/{param}")
  public Response.Content view(String param) {
    return Response.ok("pass=" + param);
  }
}