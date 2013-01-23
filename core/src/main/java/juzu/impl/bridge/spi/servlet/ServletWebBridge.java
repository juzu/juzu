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

package juzu.impl.bridge.spi.servlet;

import juzu.asset.AssetLocation;
import juzu.impl.bridge.spi.web.WebBridge;
import juzu.impl.common.Tools;
import juzu.impl.inject.ScopedContext;
import juzu.request.ClientContext;
import juzu.request.HttpContext;
import juzu.request.UserContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletWebBridge implements WebBridge, HttpContext, ClientContext, UserContext {

  /** . */
  private final HttpServletRequest req;

  /** . */
  private final HttpServletResponse resp;

  /** . */
  private final String requestPath;

  /** . */
  private final String path;

  public ServletWebBridge(HttpServletRequest req, HttpServletResponse resp, String path) {
    this.req = req;
    this.resp = resp;
    this.requestPath = req.getRequestURI().substring(req.getContextPath().length());
    this.path = path;
  }

  public HttpServletRequest getRequest() {
    return req;
  }

  public HttpServletResponse getResponse() {
    return resp;
  }

  // HttpBridge implementation

  public Map<String, String[]> getParameters() {
    return req.getParameterMap();
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

  public void renderRequestURL(Appendable appendable) throws IOException {
    appendable.append(req.getScheme());
    appendable.append("://");
    appendable.append(req.getServerName());
    int port = req.getServerPort();
    if (port != 80) {
      appendable.append(':').append(Integer.toString(port));
    }
    appendable.append(req.getContextPath());
    appendable.append(path);
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
      case CLASSPATH:
        appendable.append(req.getContextPath()).append("/assets");
        if (!uri.startsWith("/")) {
          appendable.append('/');
        }
        appendable.append(uri);
        break;
      case URL:
        appendable.append(uri);
        break;
      default:
        throw new AssertionError();
    }
  }

  public ClientContext getClientContext() {
    return this;
  }

  public HttpContext getHttpContext() {
    return this;
  }

  public UserContext getUserContext() {
    return this;
  }

  public ScopedContext getRequestScope(boolean create) {
    ScopedContext context = (ScopedContext)req.getAttribute("juzu.request_scope");
    if (context == null && create) {
      req.setAttribute("juzu.request_scope", context = new ScopedContext());
    }
    return context;
  }

  public ScopedContext getFlashScope(boolean create) {
    ScopedContext context = null;
    HttpSession session = req.getSession(create);
    if (session != null) {
      context = (ScopedContext)session.getAttribute("juzu.flash_scope");
      if (context == null && create) {
        session.setAttribute("juzu.flash_scope", context = new ScopedContext());
      }
    }
    return context;
  }

  public ScopedContext getSessionScope(boolean create) {
    ScopedContext context = null;
    HttpSession session = req.getSession(create);
    if (session != null) {
      context = (ScopedContext)session.getAttribute("juzu.session_scope");
      if (context == null && create) {
        session.setAttribute("juzu.session_scope", context = new ScopedContext());
      }
    }
    return context;
  }

  public void purgeSession() {
    HttpSession session = req.getSession(false);
    if (session != null) {
      for (String key : Tools.list(session.getAttributeNames())) {
        session.removeAttribute(key);
      }
    }
  }

  public void setHeader(String name, String value) {
    resp.setHeader(name, value);
  }

  public void sendRedirect(String location) throws IOException {
    resp.sendRedirect(location);
  }

  public void setContentType(String contentType) {
    resp.setContentType(contentType);
  }

  public void setStatus(int status) {
    resp.setStatus(status);
  }

  public Writer getWriter() throws IOException {
    return resp.getWriter();
  }

  public OutputStream getOutputStream() throws IOException {
    return resp.getOutputStream();
  }

// ClientContext implementation

  public String getContentType() {
    return req.getContentType();
  }

  public String getCharacterEncoding() {
    return req.getCharacterEncoding();
  }

  public int getContentLenth() {
    return req.getContentLength();
  }

  public InputStream getInputStream() throws IOException {
    return req.getInputStream();
  }

  // HttpContext implementation

  public String getMethod() {
    return req.getMethod();
  }

  public Cookie[] getCookies() {
    return req.getCookies();
  }

  public String getScheme() {
    return req.getScheme();
  }

  public int getServerPort() {
    return req.getServerPort();
  }

  public String getServerName() {
    return req.getServerName();
  }

  public String getContextPath() {
    return req.getContextPath();
  }

  // UserContext implementation

  public Locale getLocale() {
    return req.getLocale();
  }

  public Iterable<Locale> getLocales() {
    return new Iterable<Locale>() {
      public Iterator<Locale> iterator() {
        return new Iterator<Locale>() {
          Enumeration<Locale> e = req.getLocales();
          public boolean hasNext() {
            return e.hasMoreElements();
          }
          public Locale next() {
            return e.nextElement();
          }
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
}
