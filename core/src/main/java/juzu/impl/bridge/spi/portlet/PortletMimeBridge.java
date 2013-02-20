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
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.spi.MimeBridge;
import juzu.impl.common.Formatting;
import juzu.io.AppendableStream;
import juzu.io.BinaryOutputStream;
import juzu.io.Stream;

import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import java.io.IOException;
import java.io.PrintWriter;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class PortletMimeBridge<Rq extends PortletRequest, Rs extends MimeResponse> extends PortletRequestBridge<Rq, Rs> implements MimeBridge {

  PortletMimeBridge(Bridge bridge, Rq request, Rs response, PortletConfig config) {
    super(bridge, request, response, config);
  }

  @Override
  public void send() throws IOException, PortletException {
    if (response instanceof Response.Content<?>) {

      //
      Response.Content<?> content = (Response.Content<?>)response;

      //
      String mimeType = content.getMimeType();
      if (mimeType != null) {
        this.resp.setContentType(mimeType);
      }

      // Send properties
      sendProperties();

      // Send content
      if (content.getKind() == Stream.Char.class) {
        ((Response.Content<Stream.Char>)content).send(new AppendableStream(this.resp.getWriter()));
      }
      else {
        ((Response.Content<Stream.Binary>)content).send(new BinaryOutputStream(this.resp.getPortletOutputStream()));
      }
    } else if (response instanceof Response.Error) {
      Response.Error error = (Response.Error)response;
      if (bridge.module.context.getRunMode().getPrettyFail()) {
        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();
        writer.append("<div class=\"juzu\">");
        Throwable cause = error.getCause();
        if (cause != null) {
          Formatting.renderThrowable(null, writer, cause);
        } else {
          writer.append(error.getMessage());
        }
        writer.append("</div>");
        writer.close();
      } else {
        throw new PortletException(error.getCause());
      }
    }
  }
}
