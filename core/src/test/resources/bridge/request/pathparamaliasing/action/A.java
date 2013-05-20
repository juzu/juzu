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

package bridge.request.pathparamaliasing.action;

import juzu.Action;
import juzu.Param;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.impl.bridge.request.AbstractRequestPathParamAliasing;
import juzu.request.RequestContext;
import juzu.request.RequestLifeCycle;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A implements RequestLifeCycle {

  /** . */
  RequestContext context;

  public void beginRequest(RequestContext context) {
    this.context = context;
  }

  public void endRequest(RequestContext context) {
    this.context = null;
  }

  @View
  public Response.Content index() {
    return Response.ok("<a id='trigger' href='" + A_.foo("abc_value") + "'>click</div>");
  }

  @Action
  @Route("/foo/{a.b.c}")
  public Response.View foo(@Param(name = "a.b.c") String abc) {
    AbstractRequestPathParamAliasing.value = abc;
    return A_.index();
  }
}
