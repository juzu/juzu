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

package juzu.test.protocol.http;

import juzu.Response;
import juzu.impl.bridge.spi.ResourceBridge;
import juzu.impl.common.Logger;
import juzu.impl.common.MethodHandle;
import juzu.impl.common.Tools;
import juzu.impl.plugin.application.ApplicationLifeCycle;
import juzu.io.Streams;
import juzu.io.BinaryOutputStream;
import juzu.io.Stream;
import juzu.request.ClientContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ResourceBridgeImpl extends MimeBridgeImpl implements ResourceBridge {

  /** . */
  private Response.Content response;

  ResourceBridgeImpl(
      Logger log,
      ApplicationLifeCycle<?, ?> application,
      HttpServletRequest req,
      HttpServletResponse resp,
      MethodHandle target,
      Map<String, String[]> parameters) {
    super(log, application, req, resp, target, parameters);
  }

  public ClientContext getClientContext() {
    return this;
  }

  public void setResponse(Response response) throws IllegalStateException, IOException {
    if (response instanceof Response.Content) {
      this.response = (Response.Content)response;
    } else {
      throw new IllegalArgumentException();
    }
  }

  public void close() {
    if (response != null) {
      //
      int status = response.getStatus();
      if (status != 200) {
        resp.setStatus(status);
      }

      // Set mime type
      String mimeType = response.getMimeType();
      if (mimeType != null) {
        resp.setContentType(mimeType);
      }

      // Send response
      try {
        if (response.getKind() == Stream.Char.class) {
          response.send(Streams.closeable(resp.getWriter()));
        }
        else {
          OutputStream out = resp.getOutputStream();
          try {
            response.send(new BinaryOutputStream(out));
          }
          finally {
            Tools.safeClose(out);
          }
        }
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
