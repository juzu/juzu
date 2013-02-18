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

package plugin.controller.lifecycle.dispatch;

import juzu.Response;
import juzu.View;
import juzu.request.RequestContext;
import juzu.request.RequestLifeCycle;
import juzu.test.Registry;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A implements RequestLifeCycle {

  public void beginRequest(RequestContext context) {
    Registry.compareAndSet("count", null, 0);
  }

  @View
  public Response.Render index() {
    Registry.compareAndSet("count", 0, 1);
    return Response.ok("index");
  }

  public void endRequest(RequestContext context) {
    Registry.compareAndSet("count", 1, 2);
  }
}
