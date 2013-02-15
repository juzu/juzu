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

package juzu.impl.plugin.controller;

import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.plugin.controller.descriptor.ControllersDescriptor;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.Method;
import juzu.impl.metadata.Descriptor;
import juzu.impl.common.JSON;
import juzu.impl.request.Parameter;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.request.ActionContext;
import juzu.request.ApplicationContext;
import juzu.request.ClientContext;
import juzu.request.HttpContext;
import juzu.request.MimeContext;
import juzu.request.RenderContext;
import juzu.request.RequestContext;
import juzu.request.ResourceContext;
import juzu.request.SecurityContext;
import juzu.request.UserContext;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerPlugin extends ApplicationPlugin implements RequestFilter {

  /** . */
  private ControllersDescriptor descriptor;

  public ControllerPlugin() {
    super("controller");
  }

  public ControllersDescriptor getDescriptor() {
    return descriptor;
  }

  public ControllerResolver<Method> getResolver() {
    return descriptor != null ? descriptor.getResolver() : null;
  }

  @Override
  public Descriptor init(ClassLoader loader, JSON config) throws Exception {
    return descriptor = new ControllersDescriptor(loader, config);
  }

  public void invoke(Request request) {
    // Inject RequestContext in the arguments
    RequestContext context = request.getContext();
    Method<?> method = context.getMethod();
    for (Parameter parameter : method.getParameters()) {
      if (parameter instanceof ContextualParameter) {
        ContextualParameter contextualParameter = (ContextualParameter)parameter;
        tryInject(request, contextualParameter, RequestContext.class, context);
        tryInject(request, contextualParameter, HttpContext.class, context.getHttpContext());
        tryInject(request, contextualParameter, SecurityContext.class, context.getSecurityContext());
        tryInject(request, contextualParameter, ApplicationContext.class, context.getApplicationContext());
        tryInject(request, contextualParameter, UserContext.class, context.getUserContext());
        if (context instanceof ResourceContext) {
          ResourceContext resourceContext = (ResourceContext)context;
          tryInject(request, contextualParameter, ClientContext.class, resourceContext.getClientContext());
        } else if (context instanceof ActionContext) {
          ActionContext actionContext = (ActionContext)context;
          tryInject(request, contextualParameter, ClientContext.class, actionContext.getClientContext());
        }
      }
    }
    request.invoke();
  }

  private <T> void tryInject(Request request, ContextualParameter parameter, Class<T> type, T instance) {
    if (instance != null && type.isAssignableFrom(parameter.getType())) {
      request.setArgument(parameter, instance);
    }
  }
}
