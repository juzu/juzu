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

package juzu.request;

import juzu.Response;
import juzu.impl.plugin.application.ApplicationContext;
import juzu.impl.plugin.controller.descriptor.MethodDescriptor;
import juzu.impl.request.Request;
import juzu.impl.bridge.spi.ActionBridge;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ActionContext extends RequestContext {

  /** . */
  private ActionBridge bridge;

  public ActionContext(Request request, ApplicationContext application, MethodDescriptor method, ActionBridge bridge) {
    super(request, application, method);

    //
    this.bridge = bridge;
  }

  @Override
  protected ActionBridge getBridge() {
    return bridge;
  }

  public ClientContext getClientContext() {
    return bridge.getClientContext();
  }

  @Override
  public Phase getPhase() {
    return Phase.ACTION;
  }

  public Response.Update createResponse(MethodDescriptor<Phase.View> target) throws IllegalStateException {
    return createViewDispatch(target);
  }

  public Response.Update createResponse(MethodDescriptor<Phase.View> target, Object arg) throws IllegalStateException {
    return createViewDispatch(target, arg);
  }

  public Response.Update createResponse(MethodDescriptor<Phase.View> target, Object[] args) throws IllegalStateException {
    return createViewDispatch(target, args);
  }
}
