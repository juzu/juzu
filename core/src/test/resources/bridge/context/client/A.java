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

package bridge.context.client;

import juzu.Action;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.impl.bridge.context.AbstractContextClientTestCase;
import juzu.impl.common.Tools;
import juzu.request.ClientContext;

import java.io.IOException;
import java.io.InputStream;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  @Action
  @Route("/action")
  public Response.View action(ClientContext clientContext) throws IOException {
    test(clientContext);
    AbstractContextClientTestCase.kind = "action";
    return A_.index();
  }

  @Resource
  @Route("/resource")
  public Response.Status resource(ClientContext clientContext) throws IOException {
    test(clientContext);
    AbstractContextClientTestCase.kind = "resource";
    return Response.ok();
  }

  private void test(ClientContext client) throws IOException {
    InputStream in = client.getInputStream();
    AbstractContextClientTestCase.content = Tools.read(in, Tools.UTF_8);
    AbstractContextClientTestCase.contentLength = client.getContentLenth();
    AbstractContextClientTestCase.charset = client.getCharacterEncoding();
    AbstractContextClientTestCase.contentType = client.getContentType();
  }

  @View
  @Route("/index")
  public Response.Content index() {
    return Response.ok(
        "<a id='action' href='" + A_.action() + "'>link</a>" +
        "<a id='resource' href='" + A_.resource() + "'>link</a>"
    );
  }
}
