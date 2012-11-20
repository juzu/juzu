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

package juzu.impl.bridge.spi.portlet;

import juzu.Response;
import juzu.impl.plugin.application.ApplicationContext;
import juzu.impl.bridge.spi.MimeBridge;
import juzu.io.AppendableStream;
import juzu.io.BinaryOutputStream;
import juzu.io.Stream;

import javax.portlet.MimeResponse;
import javax.portlet.PortletRequest;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class PortletMimeBridge<Rq extends PortletRequest, Rs extends MimeResponse> extends PortletRequestBridge<Rq, Rs> implements MimeBridge {

  /** . */
  private Response.Content<?> response;

  PortletMimeBridge(ApplicationContext application, Rq request, Rs response, boolean prod) {
    super(application, request, response, prod);
  }

  public void setResponse(Response response) throws IllegalStateException, IOException {
    super.setResponse(response);
    if (response instanceof Response.Content<?>) {
      this.response = (Response.Content<?>)response;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void send() throws IOException {
    if (response != null) {
      //
      String mimeType = response.getMimeType();
      if (mimeType != null) {
        this.resp.setContentType(mimeType);
      }

      // Set headers
      for (Map.Entry<String, String[]> entry : responseHeaders.entrySet()) {
        for (String value : entry.getValue()) {
          resp.addProperty(entry.getKey(), value);
        }
      }

      // Send content
      if (response.getKind() == Stream.Char.class) {
        ((Response.Content<Stream.Char>)response).send(new AppendableStream(this.resp.getWriter()));
      }
      else {
        ((Response.Content<Stream.Binary>)response).send(new BinaryOutputStream(this.resp.getPortletOutputStream()));
      }
    }
  }
}
