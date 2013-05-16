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

package plugin.controller.resource.throwable.checked;

import juzu.Resource;
import juzu.Response;
import juzu.View;

import javax.naming.AuthenticationException;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  public A() {
  }

  @Resource
  public void resource() throws AuthenticationException {
    throw new AuthenticationException();
  }

  @View
  public Response.Content index() throws IOException {
    return Response.ok(A_.resource().toString());
  }
}
