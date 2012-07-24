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

package juzu.test.protocol.mock;

import juzu.request.HttpContext;

import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockHttpContext implements HttpContext {

  /** . */
  private static final Pattern CONTEXT_PATH_PATTERN = Pattern.compile("(?:/.*[^/])?");

  /** . */
  private static final Pattern FQDN_PATTERN = Pattern.compile("([a-zA-Z]+(([\\w-]+)*[\\w]+)*)+(\\.[a-zA-Z]+(([\\w-]+)*[\\w]+)*)*");

  /** . */
  private ArrayList<Cookie> cookies;

  /** . */
  private String scheme;

  /** . */
  private int serverPort;

  /** . */
  private String serverName;

  /** . */
  private String contextPath;

  public MockHttpContext() {
    this.cookies = new ArrayList<Cookie>();
    this.scheme = "http";
    this.serverPort = 80;
    this.serverName = "localhost";
    this.contextPath = "";
  }

  public Cookie[] getCookies() {
    Cookie[] c = new Cookie[cookies.size()];
    for (int i = 0;i < cookies.size();i++) {
      c[i] = (Cookie)cookies.get(i).clone();
    }
    return c;
  }

  public void addCookie(Cookie cookie) {
    cookies.add((Cookie)cookie.clone());
  }

  public void clearCookies() {
    cookies.clear();
  }

  public String getScheme() {
    return scheme;
  }

  public void setScheme(String scheme) {
    if (scheme == null) {
      throw new NullPointerException("No null scheme value accepted");
    }
    if (!"http".equals(scheme) && !"https".equals(scheme)) {
      throw new IllegalArgumentException("Scheme " + scheme + " is not valid");
    }
    this.scheme = scheme;
  }

  public int getServerPort() {
    return serverPort;
  }

  public void setServerPort(int serverPort) {
    if (serverPort < 1) {
      throw new IllegalArgumentException("Port " + serverPort + " is not valid");
    }
    this.serverPort = serverPort;
  }

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    if (serverName == null) {
      throw new NullPointerException("No null server name accepted");
    }
    if (!FQDN_PATTERN.matcher(serverName).matches()) {
      throw new IllegalArgumentException("Illegal server name value " + serverName);
    }
    this.serverName = serverName;
  }

  public String getContextPath() {
    return contextPath;
  }

  public void setContextPath(String contextPath) {
    if (contextPath == null) {
      throw new NullPointerException("No null context path accepted");
    }
    if (!CONTEXT_PATH_PATTERN.matcher(contextPath).matches()) {
      throw new IllegalArgumentException("Illegal context path value " + contextPath);
    }
    this.contextPath = contextPath;
  }
}
