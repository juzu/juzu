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
import juzu.impl.bridge.ViewStreamable;
import juzu.impl.common.Formatting;
import juzu.impl.common.Tools;
import juzu.impl.bridge.spi.ScopedContext;
import juzu.request.ApplicationContext;
import juzu.request.RequestParameter;
import juzu.io.Stream;
import juzu.request.ClientContext;
import juzu.request.HttpContext;
import juzu.request.UserContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class WebBridge {

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
      if (headers != null) {
        for (Map.Entry<String, String[]> entry : headers) {
          setHeader(entry.getKey(), entry.getValue()[0]);
        }
      }

      //
      Stream stream = getStream(charset);

      // Send response
      ViewStreamable vs = new ViewStreamable(body) {
        @Override
        public void renderAssetURL(AssetLocation location, String uri, Appendable appendable) throws IOException {
          WebBridge.this.renderAssetURL(location, uri, appendable);
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
        if (headers != null) {
          for (Map.Entry<String, String[]> entry : headers) {
            setHeader(entry.getKey(), entry.getValue()[0]);
          }
        }
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

  //

  public abstract void renderRequestURL(Appendable appendable) throws IOException;

  public abstract void renderAssetURL(AssetLocation location, String uri, Appendable appendable) throws IOException;

  //

  public abstract ScopedContext getRequestScope(boolean create);

  public abstract ScopedContext getFlashScope(boolean create);

  public abstract ScopedContext getSessionScope(boolean create);

  public abstract void purgeSession();

  //

  public abstract HttpContext getHttpContext();

  public abstract ClientContext getClientContext();

  public abstract UserContext getUserContext();

  public abstract ApplicationContext getApplicationContext();

  //

  public abstract void setContentType(String mimeType, Charset charset);

  public abstract void setStatus(int status);

  public abstract void setHeader(String name, String value);

  public abstract void sendRedirect(String location) throws IOException;

  public abstract Stream getStream(Charset charset) throws IOException;



//  void setHeader();
//
//  void sendRedirect(String location) throws IOException;
//
//  void send(int status);
//
//  void sendError(int status);
//
//  void renderBaseURL(Appendable appendable) throws IOException;



}
