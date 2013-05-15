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
import juzu.impl.asset.Asset;
import juzu.asset.AssetLocation;
import juzu.impl.common.Formatting;
import juzu.impl.common.Tools;
import juzu.impl.inject.ScopedContext;
import juzu.request.ApplicationContext;
import juzu.request.RequestParameter;
import juzu.io.Stream;
import juzu.request.ClientContext;
import juzu.request.HttpContext;
import juzu.request.UserContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class WebBridge {

  /** . */
  public static final PropertyType<Asset> STYLESHEET = new PropertyType<Asset>() {};

  /** . */
  public static final PropertyType<Asset> SCRIPT = new PropertyType<Asset>() {};

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
      send(Response.content(500, buffer.getBuffer()).withMimeType("text/html"), true);
    } else {
      // For now only that
      setStatus(500);
    }
  }

  public final Stream.Char send(Response.Content<Stream.Char> content, boolean decorated) throws IOException {

    //
    PropertyMap properties = content.getProperties();

    //
    Integer status = content.getStatus();
    if (status != null) {
      setStatus(status);
    }

    //
    setContentType(content.getMimeType());
    Iterable<Map.Entry<String, String[]>> headers = properties.getValues(PropertyType.HEADER);
    if (headers != null) {
      for (Map.Entry<String, String[]> entry : headers) {
        setHeader(entry.getKey(), entry.getValue()[0]);
      }
    }

    //
    Stream.Char stream = getOutputStream();

    // Send page header
    if (decorated) {
      sendHeader(properties, stream);
    }

    // Send response
    if (content.getKind() == Stream.Char.class) {
      ViewStreamable vs = new ViewStreamable(content.getStreamable(), decorated);
      try {
        vs.send(stream);
      }
      finally {
        end(stream);
      }
    } else {
      stream.close();
      throw new UnsupportedOperationException("Not yet handled");
    }

    //
    return stream;
  }

  protected void end(Stream.Char stream) {
    // Do nothing by default
  }

  private void sendHeader(PropertyMap properties, Stream.Char writer) throws IOException {
    //
    writer.append("<!DOCTYPE html>\n");
    writer.append("<html>\n");
    writer.append("<head>\n");

    //
    String title = properties.getValue(PropertyType.TITLE);
    if (title != null) {
      writer.append("<title>");
      writer.append(title);
      writer.append("</title>\n");
    }

    //
    Iterable<Map.Entry<String, String>> metaProps = properties.getValues(PropertyType.META_TAG);
    if (metaProps != null) {
      for (Map.Entry<String, String> meta : metaProps) {
        writer.append("<meta name=\"");
        writer.append(meta.getKey());
        writer.append("\" content=\"");
        writer.append(meta.getValue());
        writer.append("\">\n");
      }
    }

    //
    Iterable<Asset> stylesheets = properties.getValues(STYLESHEET);
    if (stylesheets != null) {
      for (Asset stylesheet : stylesheets) {
        String path = stylesheet.getURI();
        int pos = path.lastIndexOf('.');
        String ext = pos == -1 ? "css" : path.substring(pos + 1);
        writer.append("<link rel=\"stylesheet\" type=\"text/");
        writer.append(ext);
        writer.append("\" href=\"");
        renderAssetURL(stylesheet.getLocation(), stylesheet.getURI(), writer);
        writer.append("\"></link>\n");
      }
    }

    //
    Iterable<Asset> scripts = properties.getValues(SCRIPT);
    if (scripts != null) {
      for (Asset script : scripts) {
        writer.append("<script type=\"text/javascript\" src=\"");
        renderAssetURL(script.getLocation(), script.getURI(), writer);
        writer.append("\"></script>\n");
      }
    }

    //
    writer.append("</head>\n");
    writer.append("<body>\n");
  }

  //

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

  public abstract void setContentType(String contentType);

  public abstract void setStatus(int status);

  public abstract void setHeader(String name, String value);

  public abstract void sendRedirect(String location) throws IOException;

  public abstract Stream.Char getOutputStream() throws IOException;



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
