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

package actionformparam;

import juzu.Action;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.bridge.vertx.ActionFormParamTestCase;

public class A {

  @Action
  @Route("/action")
  public Response.View action(String param) {
    ActionFormParamTestCase.action = param;
    return A_.view();
  }

  @View
  @Route("/view")
  public Response.Content view() {
    return Response.ok("pass");
  }
}