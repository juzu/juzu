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

package plugin.binding.provider.qualified;

import juzu.Response;
import juzu.View;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  @Inject
  @Named("foo")
  Service fooService;

  @Inject
  @Named("bar")
  Service barService;

  @View
  public Response.Content index() throws IOException {
    String resp;
    if (fooService == null) {
      resp = "failed: no foo service";
    }
    else {
      String name = fooService.getName();
      if ("foo".equals(name)) {
        if (barService == null) {
          resp = "failed: no bar service";
        }
        else {
          name = barService.getName();
          if ("bar".equals(name)) {
            resp = "pass";
          }
          else {
            resp = "failed: wrong bar name " + name;
          }
        }
      }
      else {
        resp = "failed: wrong foo name " + name;
      }
    }
    return Response.ok(resp);
  }
}
