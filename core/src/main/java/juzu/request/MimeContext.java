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

package juzu.request;

import juzu.Response;
import juzu.impl.request.Method;
import juzu.impl.request.Request;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MimeContext extends RequestContext {

  protected MimeContext(Request request, Method method) {
    super(request, method);
  }

  public void setResponse(Response.Content response) throws IOException, IllegalStateException {
    // Consume response here
//    StringBuilder buffer = new StringBuilder();
//    AppendableStream printer = new AppendableStream(buffer);
//    response.send(printer);
//    if (response instanceof Response.Content.Render) {
//      response = Response.render(((Response.Content.Render)response).getTitle(), buffer.toString());
//    }
//    else {
//      response = Response.content(response.getStatus(), buffer.toString());
//    }

    //
    request.setResponse(response);
  }
}
