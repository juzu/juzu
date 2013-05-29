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
package juzu.impl.bridge.spi.web;

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.asset.AssetLocation;
import juzu.impl.common.Formatting;
import juzu.impl.common.Tools;
import juzu.impl.compiler.CompilationException;
import juzu.io.Stream;
import juzu.request.RequestParameter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Map;

/** @author Julien Viet */
public abstract class WebRequestContext {

  public final void send(CompilationException e) throws IOException {
    StringWriter writer = new StringWriter();
    PrintWriter printer = new PrintWriter(writer);
    Formatting.renderErrors(printer, e.getErrors());
    send(Response.error(writer.getBuffer().toString()), true);
  }

  public final void send(Response.Error error, boolean verbose) throws IOException {
    if (verbose) {
      StringWriter buffer = new StringWriter();
      PrintWriter writer = new PrintWriter(buffer);
      Formatting.renderStyleSheet(writer);
      writer.append("<div class=\"juzu\">");
      Throwable cause = error.getCause();
      if (cause != null) {
        Formatting.renderThrowable(null, writer, cause);
      } else {
        writer.append(error.getMessage());
      }
      writer.append("</div>");
      writer.close();
      send(Response.content(500, buffer.getBuffer()).withMimeType("text/html"));
    } else {
      // For now only that
      setStatus(500);
    }
  }

  public final void send(Response.Status response) throws IOException {

    //
    PropertyMap properties = response.getProperties();

    //
    Integer status = response.getCode();
    if (status != null) {
      setStatus(status);
    }

    //
    if (response instanceof Response.Body) {

      //
      Response.Body body = (Response.Body)response;

      //
      Charset charset = body.getCharset();
      if (charset == null) {
        charset = Tools.ISO_8859_1;
      }
      setContentType(body.getMimeType(), charset);

      // Send headers
      Iterable<Map.Entry<String, String[]>> headers = properties.getValues(PropertyType.HEADER);
      if (headers == null) {
        headers = Tools.emptyIterable();
      }
      setHeaders(headers);

      //
      Stream stream = getStream(charset);

      // Send response
      juzu.impl.bridge.ViewStreamable vs = new juzu.impl.bridge.ViewStreamable(body) {
        @Override
        public void renderAssetURL(AssetLocation location, String uri, Appendable appendable) throws IOException {
          WebRequestContext.this.renderAssetURL(location, uri, appendable);
        }
      };
      try {
        vs.send(stream);
      }
      finally {
        end(stream);
      }
    } else {

      try {
        // Send headers
        Iterable<Map.Entry<String, String[]>> headers = properties.getValues(PropertyType.HEADER);
        if (headers == null) {
          headers = Tools.emptyIterable();
        }
        setHeaders(headers);
      }
      finally {
        end();
      }
    }
  }

  protected void end(Stream stream) {
    // Do nothing by default
  }

  protected void end() {
    // Do nothing by default
  }

  public abstract Map<String, RequestParameter> getParameters();

  public abstract String getRequestURI();

  public abstract String getPath();

  public abstract String getRequestPath();

  public abstract void setContentType(String mimeType, Charset charset);

  public abstract void setStatus(int status);

  public abstract void setHeaders(Iterable<Map.Entry<String, String[]>> headers);

  public abstract void sendRedirect(String location) throws IOException;

  public abstract Stream getStream(Charset charset) throws IOException;

  public abstract void renderAssetURL(AssetLocation location, String uri, Appendable appendable) throws IOException;
}
