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

package juzu.bridge.vertx;

import juzu.Method;
import juzu.asset.AssetLocation;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.spi.servlet.ServletScopedContext;
import juzu.impl.bridge.spi.web.WebBridge;
import juzu.impl.common.Lexers;
import juzu.impl.common.Logger;
import juzu.impl.bridge.spi.ScopedContext;
import juzu.impl.common.Tools;
import juzu.io.Stream;
import juzu.request.ApplicationContext;
import juzu.request.ClientContext;
import juzu.request.HttpContext;
import juzu.request.RequestParameter;
import juzu.request.UserContext;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class VertxWebBridge extends WebBridge implements HttpContext {

  /** . */
  private static final ApplicationContext APPLICATION_CONTEXT = new ApplicationContext() {
    public ResourceBundle resolveBundle(Locale locale) {
      return null;
    }
  };

  /** . */
  private final Application application;

  /** . */
  private final Logger log;

  /** . */
  private ScopedContext requestScope;

  /** . */
  private final HttpServerRequest req;

  /** . */
  private Stream writer;

  /** . */
  private Map<String, RequestParameter> parameters;

  /** . */
  private Buffer buffer;

  /** . */
  private final Method method;

  /** . */
  private final Bridge bridge;

  /** . */
  CookieScopeContext[] cookieScopes;

  public VertxWebBridge(Bridge bridge, Application application, HttpServerRequest req, Buffer buffer, Logger log) {

    //
    this.application = application;
    this.requestScope = null;
    this.req = req;
    this.writer = null;
    this.log = log;
    this.parameters = null;
    this.buffer = buffer;
    this.method = Method.valueOf(req.method);
    this.bridge = bridge;
    this.cookieScopes = new CookieScopeContext[2];
  }

  void handle(juzu.impl.bridge.spi.web.Handler handler) {
    try {
      handler.handle(this);
    }
    catch (Throwable throwable) {
      throwable.printStackTrace();
      req.response.statusCode = 500;
      req.response.end();
      req.response.close();
    }
  }

  @Override
  protected void end() {
    req.response.end();
    req.response.close();
  }

  @Override
  public Map<String, RequestParameter> getParameters() {
    if (parameters == null) {
      if (req.query != null) {
        parameters = Lexers.parseQuery(req.query);
      } else {
        parameters = buffer != null ? new HashMap<String, RequestParameter>() : Collections.<String, RequestParameter>emptyMap();
      }
      if (buffer != null) {
        for (Iterator<RequestParameter> i = Lexers.queryParser(buffer.toString());i.hasNext();) {
          RequestParameter parameter = i.next();
          parameter.appendTo(parameters);
        }
      }
    }
    return parameters;
  }

  public String getRequestURI() {
    return "/";
  }

  public String getPath() {
    return "/";
  }

  public String getRequestPath() {
    return req.path;
  }

  public void renderRequestURL(Appendable appendable) throws IOException {
    appendable.append("http://localhost:8080");
  }

  @Override
  public void renderAssetURL(AssetLocation location, String uri, Appendable appendable) throws IOException {
    switch (location) {
      case APPLICATION:
        if (!uri.startsWith("/")) {
          appendable.append('/');
        }
        appendable.append(uri);
        break;
      case URL:
        appendable.append(uri);
        break;
      default:
        throw new UnsupportedOperationException("todo");
    }
  }

  public ScopedContext getRequestScope(boolean create) {
    if (requestScope == null && create) {
      requestScope = new ServletScopedContext(log);
    }
    return requestScope;
  }

  CookieScopeContext getCookieScopeContext(int type, boolean create) {
    if (create && cookieScopes[type] == null) {
      cookieScopes[type] = new CookieScopeContext();
    }
    return cookieScopes[type];
  }

  public ScopedContext getFlashScope(boolean create) {
    return getCookieScopeContext(CookieScopeContext.FLASH, create);
  }

  public ScopedContext getSessionScope(boolean create) {
    return getCookieScopeContext(CookieScopeContext.SESSION, create);
  }

  public void purgeSession() {
    throw new UnsupportedOperationException("todo");
  }

  public HttpContext getHttpContext() {
    return this;
  }

  public ClientContext getClientContext() {
    throw new UnsupportedOperationException("todo");
  }

  public ApplicationContext getApplicationContext() {
    return APPLICATION_CONTEXT;
  }

  public void setContentType(String mimeType, Charset charset) {
    req.response.headers().put("Content-Type", "text/html; charset=UTF-8");
  }

  public void setStatus(int status) {
    req.response.statusCode = status;
  }

  @Override
  public void setHeaders(Iterable<Map.Entry<String, String[]>> headers) {
    // Do nothing for now but send cookies if we have
    setHeaders("flash", cookieScopes[CookieScopeContext.FLASH]);
    setHeaders("session", cookieScopes[CookieScopeContext.SESSION]);
  }

  private void setHeaders(String scopeName, CookieScopeContext scope) {
    DateFormat expiresFormat = new SimpleDateFormat("E, dd-MMM-yyyy k:m:s 'GMT'");
    String expires = expiresFormat.format(new Date(System.currentTimeMillis() + 3600 * 1000));
    if (scope != null && scope.size() > 0) {
      for (Map.Entry<String, ScopedCookie> entry : scope.entries.entrySet()) {
        ScopedCookie cookie = entry.getValue();
        switch (cookie.status) {
          case CookieScopeContext.RECEIVED:
          case CookieScopeContext.TO_SEND: {
            String name = entry.getKey();
            try {
              Serializable value = (Serializable)cookie.scoped.get();
              ByteArrayOutputStream baos = new ByteArrayOutputStream();
              Tools.serialize(value, baos);
              baos.close();
              String encoded = DatatypeConverter.printBase64Binary(baos.toByteArray());
//              HttpCookie tmp = new HttpCookie(scopeName + "." + name, encoded);
              req.response.putHeader("Set-Cookie", scopeName + "." + name + "=" + encoded + "; expires=" + expires + "; domain=localhost; path=/");
            }
            catch (Exception e) {
              log.log("Could not encode cookie", e);
            }
            break;
          }
          case CookieScopeContext.TO_REMOVE:
            req.response.putHeader("Set-Cookie", scopeName + "." + entry.getKey() + "=");
            break;
        }
      }
    }
  }

  public void sendRedirect(String location) throws IOException {
    switch (method) {
      case GET:
      case HEAD:
        setStatus(302);
        break;
      default:
        setStatus(303);
        break;
    }
    req.response.headers().put("Location", location);
    req.response.end();
    req.response.close();
  }

  @Override
  public Stream getStream(Charset charset) throws IOException {
    if (writer == null) {
      writer = new VertxStream(charset, req.response);
    }
    return writer;
  }

  @Override
  public UserContext getUserContext() {
    return VertxUserContext.INSTANCE;
  }

  // HttpContext implementation ****************************************************************************************

  public Method getMethod() {
    return method;
  }

  public javax.servlet.http.Cookie[] getCookies() {
    throw new UnsupportedOperationException();
  }

  public String getScheme() {
    return "http";
  }

  public int getServerPort() {
    return application.port;
  }

  public String getServerName() {
    throw new UnsupportedOperationException();
  }

  public String getContextPath() {
    return "/";
  }
}
