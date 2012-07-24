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

package juzu;

import juzu.request.ActionContext;
import juzu.request.HttpContext;
import juzu.request.RenderContext;
import juzu.request.RequestContext;
import juzu.request.RequestLifeCycle;
import juzu.request.ResourceContext;
import juzu.request.SecurityContext;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class Controller implements RequestLifeCycle {

  /** . */
  protected ActionContext actionContext;

  /** . */
  protected RenderContext renderContext;

  /** . */
  protected ResourceContext resourceContext;

  /** . */
  protected HttpContext httpContext;

  /** . */
  protected SecurityContext securityContext;

  public void beginRequest(RequestContext context) {
    if (context instanceof ActionContext) {
      actionContext = (ActionContext)context;
    }
    else if (context instanceof RenderContext) {
      renderContext = (RenderContext)context;
    }
    else if (context instanceof ResourceContext) {
      resourceContext = (ResourceContext)context;
    }
    httpContext = context.getHttpContext();
    securityContext = context.getSecurityContext();
  }

  public void endRequest(RequestContext context) {
    this.actionContext = null;
    this.renderContext = null;
    this.resourceContext = null;
    this.httpContext = null;
    this.securityContext = null;
  }
}
