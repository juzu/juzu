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
package juzu.impl.bridge.spi.servlet;

import juzu.asset.AssetLocation;
import juzu.impl.bridge.spi.web.HttpStream;
import juzu.impl.bridge.spi.web.WebRequestContext;
import juzu.impl.common.FormURLEncodedParser;
import juzu.impl.common.Lexers;
import juzu.impl.common.Spliterator;
import juzu.impl.common.Tools;
import juzu.impl.io.BinaryOutputStream;
import juzu.io.Stream;
import juzu.request.RequestParameter;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** @author Julien Viet */
public class ServletRequestContext extends WebRequestContext {

  /** . */
  final HttpServletRequest req;

  /** . */
  final HttpServletResponse resp;

  /** . */
  final String path;

  /** . */
  final String requestPath;

  /** . */
  final Map<String, RequestParameter> requestParameters;

  /** . */
  final Charset defaultEncoding;

  public ServletRequestContext(
      Charset defaultEncoding,
      HttpServletRequest req,
      HttpServletResponse resp,
      String path) {

    //
    Map<String, RequestParameter> requestParameters = Collections.emptyMap();
    String query = req.getQueryString();
    if (query != null) {
      for (Iterator<RequestParameter> i = Lexers.queryParser(query);i.hasNext();) {
        if (requestParameters.isEmpty()) {
          requestParameters = new HashMap<String, RequestParameter>();
        }
        RequestParameter parameter = i.next();
        parameter.appendTo(requestParameters);
      }
    }

    //
    if ("POST".equals(req.getMethod())) {
      String contentType = req.getContentType();
      if (contentType != null && contentType.length() > 0) {
        Spliterator i = new Spliterator(contentType, ';');
        if ("application/x-www-form-urlencoded".equals(i.next().trim())) {
          Charset charset = defaultEncoding;
          while (i.hasNext()) {
            String v = i.next().trim();
            if (v.startsWith("charset=")) {
              charset = Charset.forName(v.substring("charset=".length()));
            }
          }
          try {
            byte[] bytes = Tools.copy(req.getInputStream(), new ByteArrayOutputStream()).toByteArray();
            String form = new String(bytes, charset);
            FormURLEncodedParser parser = new FormURLEncodedParser(defaultEncoding, form, 0, form.length());
            for (RequestParameter parameter : parser) {
              if (requestParameters.isEmpty()) {
                requestParameters = new HashMap<String, RequestParameter>();
              }
              parameter.appendTo(requestParameters);
            }
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }

    //
    this.defaultEncoding = defaultEncoding;
    this.requestPath = req.getRequestURI().substring(req.getContextPath().length());
    this.requestParameters = requestParameters;
    this.req = req;
    this.path = path;
    this.resp = resp;
  }

  public Map<String, RequestParameter> getParameters() {
    return requestParameters;
  }

  public String getRequestPath() {
    return requestPath;
  }

  public String getPath() {
    return path;
  }

  public String getRequestURI() {
    return req.getRequestURI();
  }

  @Override
  public HttpStream getStream(int status) {
    return new ServletStream(status, defaultEncoding);
  }

  public class ServletStream extends HttpStream {

    /** . */
    private Stream dataStream;

    /** . */
    private AsyncContext context;

    ServletStream(int status, Charset encoding) {
      super(ServletRequestContext.this, status, encoding);
    }

    @Override
    public void setStatusCode(int status) {
      resp.setStatus(status);
    }

    @Override
    protected Stream getDataStream(boolean create) {
      if (dataStream == null && create) {
        try {
          dataStream = new BinaryOutputStream(encoding, resp.getOutputStream());
        }
        catch (IOException e) {
          throw new UnsupportedOperationException("Handle me gracefully", e);
        }
      }
      return dataStream;
    }

    @Override
    protected void endAsync() {
      if (context != null) {
        System.out.println("COMPLETING ASYNC");
        context.complete();
      }
    }

    @Override
    protected void beginAsync() {
      if (req.isAsyncStarted()) {
        System.out.println("DETECTED ASYNC ALREADY STARTED");
        context = req.getAsyncContext();
      } else {
        System.out.println("STARTING ASYNC");
        context = req.startAsync();
      }
    }
  }

  @Override
  public void setHeaders(Iterable<Map.Entry<String, String[]>> headers) {
    for (Map.Entry<String, String[]> header : headers) {
      resp.setHeader(header.getKey(), header.getValue()[0]);
    }
  }

  public void sendRedirect(String location) throws IOException {
    resp.sendRedirect(location);
  }

  public void setContentType(String mimeType, Charset charset) {
    String name = charset.name();
    resp.setCharacterEncoding(name);
    resp.setContentType(mimeType);
  }

  public void setStatus(int status) {
    resp.setStatus(status);
  }

  public void renderAssetURL(AssetLocation location, String uri, Appendable appendable) throws IOException {
    switch (location) {
      case SERVER:
        if (!uri.startsWith("/")) {
          appendable.append(req.getContextPath());
          appendable.append('/');
        }
        appendable.append(uri);
        break;
      case APPLICATION:
        appendable.append(req.getContextPath()).append("/assets");
        appendable.append(uri);
        break;
      case URL:
        appendable.append(uri);
        break;
      default:
        throw new AssertionError();
    }
  }
}
