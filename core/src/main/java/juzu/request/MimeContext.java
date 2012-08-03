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

package juzu.request;

import juzu.Response;
import juzu.URLBuilder;
import juzu.impl.plugin.application.ApplicationContext;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.plugin.controller.descriptor.MethodDescriptor;
import juzu.impl.request.Request;
import juzu.impl.bridge.spi.MimeBridge;
import juzu.io.AppendableStream;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MimeContext extends RequestContext {

  /** . */
  private final ApplicationContext application;

  protected MimeContext(Request request, ApplicationContext application, MethodDescriptor method) {
    super(request, application, method);

    //
    this.application = application;
  }

  @Override
  protected abstract MimeBridge getBridge();

  public URLBuilder createURLBuilder(MethodDescriptor method) {
    URLBuilder builder = new URLBuilder(getBridge(), method);

    // Bridge escape XML value
    ApplicationDescriptor desc = application.getDescriptor();
    builder.escapeXML(desc.getControllers().getEscapeXML());

    //
    return builder;
  }

  public URLBuilder createURLBuilder(MethodDescriptor method, Object arg) {
    URLBuilder builder = createURLBuilder(method);
    method.setArgs(new Object[]{arg}, builder.getParameters());
    return builder;
  }

  public URLBuilder createURLBuilder(MethodDescriptor method, Object[] args) {
    URLBuilder builder = createURLBuilder(method);
    method.setArgs(args, builder.getParameters());
    return builder;
  }

  public void setResponse(Response.Content response) throws IOException, IllegalStateException {
    // Consume response here
    StringBuilder buffer = new StringBuilder();
    AppendableStream printer = new AppendableStream(buffer);
    response.send(printer);
    if (response instanceof Response.Content.Render) {
      response = Response.render(((Response.Content.Render)response).getTitle(), buffer.toString());
    }
    else {
      response = Response.content(response.getStatus(), buffer.toString());
    }

    //
    request.setResponse(response);
  }
}
