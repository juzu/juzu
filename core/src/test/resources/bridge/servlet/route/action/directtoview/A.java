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

package bridge.servlet.route.action.directtoview;

import juzu.Action;
import juzu.PropertyType;
import juzu.Response;
import juzu.Route;
import juzu.request.RenderContext;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  @juzu.View
  public Response.Render index() {
    return Response.ok(
        "<form id='form' action='" + A_.foo() + "' method='post'>" +
            "<input id='trigger' type='submit' name='click'/>" +
            "</form>");
  }

  @Action
  @Route("/foo")
  public Response.View foo() {
    return A_.bar("juu").with(PropertyType.REDIRECT_AFTER_ACTION, false);
  }

  @juzu.View
  @Route("/bar")
  public Response.Content<?> bar(String juu, RenderContext renderContext) {
    String path = renderContext.getProperty(PropertyType.PATH);
    return Response.ok("/juzu/foo".equals(path) && "juu".equals(juu) ? "pass" : "fail");
  }
}
