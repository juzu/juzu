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

package juzu.impl.bridge.spi.web;

import juzu.PropertyType;
import juzu.Response;
import juzu.impl.plugin.application.Application;
import juzu.impl.bridge.spi.ResourceBridge;
import juzu.impl.common.Tools;
import juzu.impl.request.Method;
import juzu.io.AppendableStream;
import juzu.io.BinaryOutputStream;
import juzu.io.Stream;
import juzu.request.ClientContext;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class WebResourceBridge extends WebMimeBridge implements ResourceBridge {

  WebResourceBridge(
      Application application,
      Handler handler,
      WebBridge bridge,
      Method<?> target,
      Map<String, String[]> parameters) {
    super(application, handler, bridge, target, parameters);
  }

  public ClientContext getClientContext() {
    return http.getClientContext();
  }

  boolean send() throws IOException {
    if (super.send()) {
      return true;
    } else if (response instanceof Response.Content<?>) {

      Response.Content<?> content = (Response.Content<?>)response;

      //
      int status = content.getStatus();
      if (status != 200) {
        http.setStatus(status);
      }

      // Set mime type
      String mimeType = content.getMimeType();
      if (mimeType != null) {
        http.setContentType(mimeType);
      }

      // Set headers
      Iterable<Map.Entry<String, String[]>> headers = response.getProperties().getValues(PropertyType.HEADER);
      if (headers != null) {
        for (Map.Entry<String, String[]> entry : headers) {
          http.setHeader(entry.getKey(), entry.getValue()[0]);
        }
      }

      // Send response
      if (content.getKind() == Stream.Char.class) {
        Writer writer = http.getWriter();
        try {
          ((Response.Content<Stream.Char>)content).send(new AppendableStream(writer));
        }
        finally {
          Tools.safeClose(writer);
        }
      }
      else {
        OutputStream out = http.getOutputStream();
        try {
          ((Response.Content<Stream.Binary>)content).send(new BinaryOutputStream(out));
        }
        finally {
          Tools.safeClose(out);
        }
      }
      return true;
    } else {
      return false;
    }
  }
}
