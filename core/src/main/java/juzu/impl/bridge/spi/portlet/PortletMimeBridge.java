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

package juzu.impl.bridge.spi.portlet;

import juzu.Response;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.spi.MimeBridge;
import juzu.impl.common.Formatting;
import juzu.io.Stream;
import juzu.io.Streams;

import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class PortletMimeBridge<Rq extends PortletRequest, Rs extends MimeResponse> extends PortletRequestBridge<Rq, Rs> implements MimeBridge {

  PortletMimeBridge(Bridge bridge, Rq request, Rs response, PortletConfig config) {
    super(bridge, request, response, config);
  }

  public abstract Stream createStream(String mimeType, Charset charset) throws IOException;

  @Override
  public void send() throws IOException, PortletException {
    if (response instanceof Response.Content) {

      //
      Response.Content content = (Response.Content)response;

      // Send properties
      sendProperties();

      //
      Stream stream = createStream(content.getMimeType(), content.getCharset());

      // Send content
      content.getStreamable().send(stream);
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
