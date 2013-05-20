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
package bridge.response.viewnoredirect.action;

import juzu.Action;
import juzu.PropertyType;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.impl.bridge.response.AbstractResponseViewNoRedirectActionTestCase;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  @View
  public Response.Status index() {
    AbstractResponseViewNoRedirectActionTestCase.url = A_.process().toString();
    return Response.ok();
  }

  @Action
  @Route("/process")
  public Response.View process() {
    return A_.foo("bar_value").with(PropertyType.REDIRECT_AFTER_ACTION, false)
        ;
  }

  @View
  @Route("/foo")
  public Response.Status foo(String bar) {
    AbstractResponseViewNoRedirectActionTestCase.bar = bar;
    return Response.ok();
  }
}
