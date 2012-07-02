/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package juzu.test.protocol.http;

import juzu.Response;
import juzu.impl.bridge.spi.ResourceBridge;
import juzu.impl.utils.Tools;
import juzu.io.AppendableStream;
import juzu.io.BinaryOutputStream;
import juzu.io.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ResourceBridgeImpl extends MimeBridgeImpl implements ResourceBridge {
  ResourceBridgeImpl(
      HttpServletBridgeContext context,
      HttpServletRequest req,
      HttpServletResponse resp,
      String methodId,
      Map<String, String[]> parameters) {
    super(context, req, resp, methodId, parameters);
  }

  public void end(Response response) throws IllegalStateException, IOException {
    if (response instanceof Response.Content) {
      Response.Content content = (Response.Content)response;

      //
      int status = content.getStatus();
      if (status != 200) {
        resp.setStatus(status);
      }

      // Set mime type
      String mimeType = content.getMimeType();
      if (mimeType != null) {
        resp.setContentType(mimeType);
      }

      // Send response
      if (content.getKind() == Stream.Char.class) {
        PrintWriter writer = resp.getWriter();
        try {
          content.send(new AppendableStream(writer));
        }
        finally {
          Tools.safeClose(writer);
        }
      }
      else {
        OutputStream out = resp.getOutputStream();
        try {
          content.send(new BinaryOutputStream(out));
        }
        finally {
          Tools.safeClose(out);
        }
      }
    }
  }
}
