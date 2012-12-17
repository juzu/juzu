/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package bridge.client.action;

import juzu.Action;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.impl.bridge.client.AbstractClientContextTestCase;
import juzu.impl.common.Tools;
import juzu.request.ClientContext;

import java.io.IOException;
import java.io.InputStream;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  @Action
  @Route("/action")
  public void action(ClientContext clientContext) throws IOException {
    test(clientContext);
    AbstractClientContextTestCase.kind = "action";
  }

  @Resource
  @Route("/resource")
  public void resource(ClientContext clientContext) throws IOException {
    test(clientContext);
    AbstractClientContextTestCase.kind = "resource";
  }

  private void test(ClientContext client) throws IOException {
    InputStream in = client.getInputStream();
    AbstractClientContextTestCase.content = Tools.read(in, " UTF-8");
    AbstractClientContextTestCase.contentLength = client.getContentLenth();
    AbstractClientContextTestCase.charset = client.getCharacterEncoding();
    AbstractClientContextTestCase.contentType = client.getContentType();
  }

  @View
  @Route("/index")
  public Response.Content<?> index() {
    return Response.ok(
        "<a id='action' href='" + A_.action() + "'>link</a>" +
        "<a id='resource' href='" + A_.resource() + "'>link</a>"
    );
  }
}
