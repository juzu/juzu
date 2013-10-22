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

import juzu.Method;
import juzu.impl.bridge.spi.web.WebBridge;
import juzu.impl.common.JUL;
import juzu.impl.common.Logger;
import juzu.impl.common.Tools;
import juzu.impl.bridge.spi.ScopedContext;
import juzu.request.ApplicationContext;
import juzu.request.ClientContext;
import juzu.request.HttpContext;
import juzu.request.UserContext;

import javax.servlet.AsyncContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.RejectedExecutionException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletWebBridge extends WebBridge implements HttpContext, ClientContext, UserContext {

  /** . */
  private final ServletRequestContext ctx;

  /** . */
  private final Method method;

  /** . */
  private final ServletBridge servlet;

  public ServletWebBridge(ServletBridge servlet, ServletRequestContext ctx) {
    this.ctx = ctx;
    this.method = Method.valueOf(ctx.req.getMethod());
    this.servlet = servlet;
  }

  @Override
  public Logger getLogger(String name) {
    return JUL.getLogger(name);
  }

  public ServletRequestContext getRequestContext() {
    return ctx;
  }

  public HttpServletResponse getResponse() {
    return ctx.resp;
  }

  @Override
  public void execute(final Runnable runnable) {
    if (ctx.req.isAsyncSupported()) {
      AsyncContext context = ctx.beginAsync();
      ServletRequestContext.log.trace("Scheduling runnable " + runnable);
      context.start(new Runnable() {
        public void run() {
          ServletRequestContext.log.trace("Starting runnable " + runnable);
          try {
            runnable.run();
            ServletRequestContext.log.trace("Ended runnable " + runnable);
          }
          catch (Exception e) {
            if (e instanceof InterruptedException) {
              Thread.currentThread().interrupt();
            }
            ServletRequestContext.log.trace("Failure of runnable " + runnable, e);
          }
        }
      });
    } else {
      throw new RejectedExecutionException("Async not enabled currently for this servlet");
    }
  }

  // HttpBridge implementation

  public void renderRequestURL(Appendable appendable) throws IOException {
    appendable.append(ctx.req.getScheme());
    appendable.append("://");
    appendable.append(ctx.req.getServerName());
    int port = ctx.req.getServerPort();
    if (port != 80) {
      appendable.append(':').append(Integer.toString(port));
    }
    appendable.append(ctx.req.getContextPath());
    appendable.append(ctx.path);
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

  @Override
  public ApplicationContext getApplicationContext() {
    return servlet.applicationContext;
  }

  public ScopedContext getRequestScope(boolean create) {
    ScopedContext context = (ScopedContext)ctx.req.getAttribute("juzu.request_scope");
    if (context == null && create) {
      ctx.req.setAttribute("juzu.request_scope", context = new ServletScopedContext(getLogger(ServletScopedContext.class.getName())));
    }
    return context;
  }

  public ScopedContext getFlashScope(boolean create) {
    ScopedContext context = null;
    HttpSession session = ctx.req.getSession(create);
    if (session != null) {
      context = (ScopedContext)session.getAttribute("juzu.flash_scope");
      if (context == null && create) {
        session.setAttribute("juzu.flash_scope", context = new ServletScopedContext(getLogger(ServletScopedContext.class.getName())));
      }
    }
    return context;
  }

  public ScopedContext getSessionScope(boolean create) {
    ScopedContext context = null;
    HttpSession session = ctx.req.getSession(create);
    if (session != null) {
      context = (ScopedContext)session.getAttribute("juzu.session_scope");
      if (context == null && create) {
        session.setAttribute("juzu.session_scope", context = new ServletScopedContext(getLogger(ServletScopedContext.class.getName())));
      }
    }
    return context;
  }

  public void purgeSession() {
    HttpSession session = ctx.req.getSession(false);
    if (session != null) {
      for (String key : Tools.list(session.getAttributeNames())) {
        session.removeAttribute(key);
      }
    }
  }

  // ClientContext implementation

  public String getContentType() {
    return ctx.req.getContentType();
  }

  public String getCharacterEncoding() {
    return ctx.req.getCharacterEncoding();
  }

  public int getContentLenth() {
    return ctx.req.getContentLength();
  }

  public InputStream getInputStream() throws IOException {
    return ctx.req.getInputStream();
  }

  // HttpContext implementation

  public Method getMethod() {
    return method;
  }

  public Cookie[] getCookies() {
    return ctx.req.getCookies();
  }

  public String getScheme() {
    return ctx.req.getScheme();
  }

  public int getServerPort() {
    return ctx.req.getServerPort();
  }

  public String getServerName() {
    return ctx.req.getServerName();
  }

  public String getContextPath() {
    return ctx.req.getContextPath();
  }

  // UserContext implementation

  public Locale getLocale() {
    return ctx.req.getLocale();
  }

  public Iterable<Locale> getLocales() {
    return new Iterable<Locale>() {
      public Iterator<Locale> iterator() {
        return new Iterator<Locale>() {
          Enumeration<Locale> e = ctx.req.getLocales();
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
