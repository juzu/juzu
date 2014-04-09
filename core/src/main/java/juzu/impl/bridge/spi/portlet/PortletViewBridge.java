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
import juzu.impl.common.Formatting;
import juzu.impl.compiler.CompilationException;
import juzu.io.OutputStream;
import juzu.io.Stream;
import juzu.request.ClientContext;
import juzu.request.Phase;

import javax.portlet.PortletConfig;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletViewBridge extends PortletMimeBridge<RenderRequest, RenderResponse> {

  public PortletViewBridge(Bridge bridge, RenderRequest request, RenderResponse response, PortletConfig config) {
    super(bridge, Phase.VIEW, request, response, config);
  }

  @Override
  public Stream createStream(String mimeType, Charset charset) throws IOException {
    if (mimeType != null) {
      this.resp.setContentType(mimeType);
    }

    // We use a writer during render phase as the developer may have set
    // a charset that is not the portlet container provided charset
    // and therefore it is safer to use the writer of the portlet container
    return OutputStream.create(charset, this.resp.getWriter());
  }

  public ClientContext getClientContext() {
    return null;
  }

  @Override
  public void invoke() throws Exception {
    try {
      bridge.refresh();
    }
    catch (CompilationException e) {
      StringWriter buffer = new StringWriter();
      PrintWriter printer = new PrintWriter(buffer);
      Formatting.renderErrors(printer, e.getErrors());
      setResponse(Response.error(buffer.toString()));
      return;
    }

    //
    super.invoke();
  }
}
