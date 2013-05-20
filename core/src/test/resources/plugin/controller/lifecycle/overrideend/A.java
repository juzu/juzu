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

package plugin.controller.lifecycle.overrideend;

import juzu.Response;
import juzu.View;
import juzu.request.RequestContext;
import juzu.request.RequestLifeCycle;
import juzu.test.Registry;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A implements RequestLifeCycle {

  public void beginRequest(RequestContext context) {
    Registry.compareAndSet("count", null, 0);
  }

  @View
  public Response.Content index() {
    Registry.compareAndSet("count", 0, 1);
    return Response.ok("index");
  }

  public void endRequest(RequestContext context) {
    Registry.compareAndSet("count", 1, 2);
    context.setResponse(Response.ok("end"));
  }
}
