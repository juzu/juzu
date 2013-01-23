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

package juzu.impl.bridge.spi.web;

import juzu.asset.AssetLocation;
import juzu.impl.inject.ScopedContext;
import juzu.request.ClientContext;
import juzu.request.HttpContext;
import juzu.request.UserContext;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface WebBridge {

  Map<String, String[]> getParameters();

  String getRequestURI();

  String getPath();

  String getRequestPath();

  //

  void renderRequestURL(Appendable appendable) throws IOException;

  void renderAssetURL(AssetLocation location, String uri, Appendable appendable) throws IOException;

  //

  ScopedContext getRequestScope(boolean create);

  ScopedContext getFlashScope(boolean create);

  ScopedContext getSessionScope(boolean create);

  void purgeSession();

  //

  HttpContext getHttpContext();

  ClientContext getClientContext();

  UserContext getUserContext();

  //

  void setContentType(String contentType);

  void setStatus(int status);

  void setHeader(String name, String value);

  void sendRedirect(String location) throws IOException;

  Writer getWriter() throws IOException;

  OutputStream getOutputStream() throws IOException;



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
