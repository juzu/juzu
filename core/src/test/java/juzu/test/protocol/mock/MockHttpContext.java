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

package juzu.test.protocol.mock;

import juzu.HttpMethod;
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

  public HttpMethod getMethod() {
    return HttpMethod.GET;
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
