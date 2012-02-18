/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.juzu.impl.spi.request.servlet;

import org.juzu.Response;
import org.juzu.URLBuilder;
import org.juzu.impl.spi.request.MimeBridge;
import org.juzu.metadata.ControllerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ServletMimeBridge<R extends Response.Mime> extends ServletRequestBridge<R> implements MimeBridge<R>
{

   ServletMimeBridge(HttpServletRequest req, HttpServletResponse resp, String methodId, Map<String, String[]> parameters)
   {
      super(req, resp, methodId, parameters);
   }

   public URLBuilder createURLBuilder(ControllerMethod method)
   {
      return new ServletURLBuilder(this, method);
   }
}
