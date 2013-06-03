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
import juzu.impl.bridge.spi.web.WebRequestContext;
import juzu.impl.common.Lexers;
import juzu.io.BinaryOutputStream;
import juzu.io.Stream;
import juzu.request.RequestParameter;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;
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

  public ServletRequestContext(HttpServletRequest req, HttpServletResponse resp, String path) {

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
    if ("POST".equals(req.getMethod()) && "application/x-www-form-urlencoded".equals(req.getContentType())) {
      try {
        for (Map.Entry<String, String[]> parameter : HttpUtils.parsePostData(req.getContentLength(), req.getInputStream()).entrySet()) {
          if (requestParameters.isEmpty()) {
            requestParameters = new HashMap<String, RequestParameter>();
          }
          RequestParameter requestParameter = requestParameters.get(parameter.getKey());
          if (requestParameter != null) {
            requestParameter = requestParameter.append(parameter.getValue());
          } else {
            requestParameter = RequestParameter.create(parameter);
          }
          requestParameter.appendTo(requestParameters);
        }
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }

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
  public Stream getStream(Charset charset) throws IOException {
    return new StreamImpl(charset, resp.getOutputStream());
  }

  @Override
  protected void end(Stream stream) {
    ((StreamImpl)stream).end();
  }

  class StreamImpl extends BinaryOutputStream {

    /** . */
    private boolean closed;

    /** . */
    private AsyncContext context;

    StreamImpl(Charset charset, ServletOutputStream out) {
      super(charset, out);
    }

    public void close() throws IOException {
      closed = true;
      if (context != null) {
        System.out.println("COMPLETING ASYNC");
        context.complete();
      }
    }

    void end() {
      if (req.isAsyncStarted()) {
        System.out.println("DETECTED ASYNC ALREADY STARTED");
        context = req.getAsyncContext();
      } else {
        if (!closed) {
          System.out.println("STARTING ASYNC");
          context = req.startAsync();
        }
      }
    }
  }

  @Override
  public void setHeaders(Iterable<Map.Entry<String, String[]>> headers) {
    for (Map.Entry<String, String[]> header : headers) {
      resp.setHeader(header.getKey(), header.getValue()[0]);
    }
  }

  public void setHeaders(String name, String value) {
    resp.setHeader(name, value);
  }

  public void sendRedirect(String location) throws IOException {
    resp.sendRedirect(location);
  }

  public void setContentType(String mimeType, Charset charset) {
    resp.setCharacterEncoding(charset.name());
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
