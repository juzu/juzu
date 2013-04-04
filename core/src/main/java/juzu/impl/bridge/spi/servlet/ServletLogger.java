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

import juzu.impl.common.Logger;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletLogger implements Logger {

  /** . */
  private final ServletConfig servletConfig;

  public ServletLogger(Servlet servlet) {
    this.servletConfig = servlet.getServletConfig();
  }

  public ServletLogger(ServletConfig servletConfig) {
    this.servletConfig = servletConfig;
  }

  public void log(CharSequence msg) {
    servletConfig.getServletContext().log("[" + servletConfig.getServletName() + "] " + msg);
  }

  public void log(CharSequence msg, Throwable t) {
    servletConfig.getServletContext().log("[" + servletConfig.getServletName() + "] " + msg, t);
  }
}
