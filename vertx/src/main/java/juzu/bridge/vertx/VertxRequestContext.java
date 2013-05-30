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
import juzu.impl.bridge.spi.web.WebRequestContext;
import juzu.impl.common.Lexers;
import juzu.impl.common.Logger;
import juzu.impl.common.Tools;
import juzu.impl.inject.Scoped;
import juzu.io.Stream;
import juzu.request.RequestParameter;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author Julien Viet */
public class VertxRequestContext extends WebRequestContext {

  /** . */
  private static final Pattern cookiePattern = Pattern.compile("([^=]+)=([^\\;]*);?\\s?");

  /** . */
  final HttpServerRequest req;

  /** . */
  private Map<String, RequestParameter> parameters;

  /** . */
  private final String requestPath;

  /** . */
  private final String query;

  /** . */
  private Buffer buffer;

  /** . */
  final Method method;

  /** . */
  private Stream writer;

  /** . */
  final Logger log;

  /** . */
  CookieScopeContext[] cookieScopes;

  public VertxRequestContext(HttpServerRequest req, Buffer buffer, Logger log) {

    // Compute path/query from URI - we cannot use provided request path/query as it is already decoded
    String uri = req.uri;
    int index = uri.indexOf('?');
    String requestPath;
    String query;
    if (index == -1) {
      requestPath = uri;
      query = null;
    } else {
      requestPath = uri.substring(0, index);
      query = uri.substring(index + 1);
    }

    //
    this.cookieScopes = new CookieScopeContext[2];
    this.method = Method.valueOf(req.method);
    this.requestPath = requestPath;
    this.query = query;
    this.req = req;
    this.parameters = null;
    this.buffer = buffer;
    this.log = log;

    // Parse cookies
    String cookies = req.headers().get("cookie");
    log.log("Got cookies " + cookies);
    if (cookies != null) {
      ArrayList<HttpCookie> parsed = new ArrayList<HttpCookie>();
      Matcher matcher = cookiePattern.matcher(cookies);
      while (matcher.find()) {
        String cookieKey = matcher.group(1);
        String cookieValue = matcher.group(2);
        HttpCookie cookie = new HttpCookie(cookieKey, cookieValue);
        parsed.add(cookie);
      }
      for (HttpCookie cookie : parsed) {
        String name = cookie.getName();
        String value = cookie.getValue();
        int type;
        String prefix;
        if (name.startsWith("flash.")) {
          type = CookieScopeContext.FLASH;
          prefix = "flash.";
        }
        else if (name.startsWith("session.")) {
          type = CookieScopeContext.SESSION;
          prefix = "session.";
        }
        else {
          type = -1;
          prefix = null;
        }
        if (prefix != null) {
          try {
            name = name.substring(prefix.length());
            if (value.length() > 0) {
              CookieScopeContext context = getCookieScopeContext(type, true);
              if (context.snapshot == null) {
                context.snapshot = new HashMap<String, String>();
              }
              context.snapshot.put(name, value);
            }
            else {
              // For now we consider we removed the value ...
              // We should handle proper cookie removal later
            }
          }
          catch (Exception e) {
            log.log("Could not parse cookie", e);
          }
        }
      }
    }
  }

  CookieScopeContext getCookieScopeContext(int type, boolean create) {
    if (create && cookieScopes[type] == null) {
      cookieScopes[type] = new CookieScopeContext();
    }
    return cookieScopes[type];
  }

  @Override
  protected void end() {
    req.response.end();
    req.response.close();
  }

  @Override
  public Map<String, RequestParameter> getParameters() {
    if (parameters == null) {
      if (query != null) {
        parameters = Lexers.parseQuery(query);
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
    return requestPath;
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
      if (scope.purged) {
        for (String name : scope.getNames()) {
          log.log("Clearing cookie " + name);
          req.response.putHeader("Set-Cookie", scopeName + "." + name + "=; Path=/");
        }
      } else if (scope.values != null) {
        for (Map.Entry<String, Scoped> entry : scope.values.entrySet()) {
          String name = entry.getKey();
          Serializable value = (Serializable)entry.getValue().get();
          try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Tools.serialize(value, baos);
            baos.close();
            String encoded = DatatypeConverter.printBase64Binary(baos.toByteArray());
            String request = scope.snapshot != null ? scope.snapshot.get(name) : null;
            if (encoded.equals(request)) {
              // When they are equals we don't do anything
            } else {
//              HttpCookie tmp = new HttpCookie(scopeName + "." + name, encoded);
              log.log("Sending cookie " + name + " = " + value + " as " + encoded);
              req.response.putHeader("Set-Cookie", scopeName + "." + name + "=" + encoded + "; Path=/");
            }
          }
          catch (Exception e) {
            log.log("Could not encode cookie", e);
          }
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
}
