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

package bridge.request.detypedparam.action;

import juzu.Action;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.impl.bridge.request.AbstractRequestDetypedParam;
import juzu.request.Phase;
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
    Phase.Action.Dispatch dispatch = A_.foo();
    dispatch.setParameter("detyped", "detyped_value");
    return Response.ok("<a id='trigger' href='" + dispatch + "'>click</div>");
  }

  @Action
  @Route("/foo")
  public Response.View foo() {
    AbstractRequestDetypedParam.value = context.getParameters().get("detyped").toArray();
    return A_.index();
  }
}
