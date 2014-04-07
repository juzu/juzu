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

package bridge.servlet.route.resource.method;

import juzu.HttpMethod;
import juzu.Resource;
import juzu.Response;
import juzu.Route;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  @Resource(method = HttpMethod.GET)
  @Route("/")
  public Response.Content doGet() {
    return handle("GET");
  }

  @Resource(method = HttpMethod.POST)
  @Route("/")
  public Response.Content doPost() {
    return handle("POST");
  }

  @Resource(method = HttpMethod.PUT)
  @Route("/")
  public Response.Content doPut() {
    return handle("PUT");
  }

  private Response.Content handle(String method) {
    return Response.ok("ok[" + method + "]");
  }
}
