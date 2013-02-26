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

import juzu.Response;
import juzu.UndeclaredIOException;
import juzu.impl.bridge.spi.ActionBridge;
import juzu.impl.bridge.spi.EventBridge;
import juzu.impl.bridge.spi.RenderBridge;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.bridge.spi.ResourceBridge;
import juzu.impl.common.MethodHandle;
import juzu.impl.inject.ScopeController;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.plugin.PluginContext;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.plugin.controller.descriptor.ControllersDescriptor;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.Method;
import juzu.impl.metadata.Descriptor;
import juzu.impl.request.Parameter;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.request.ActionContext;
import juzu.request.ApplicationContext;
import juzu.request.ClientContext;
import juzu.request.HttpContext;
import juzu.request.Phase;
import juzu.request.RequestContext;
import juzu.request.ResourceContext;
import juzu.request.SecurityContext;
import juzu.request.UserContext;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerPlugin extends ApplicationPlugin implements RequestFilter {

  /** . */
  private ControllersDescriptor descriptor;

  /** . */
  public ArrayList<RequestFilter> filters;

  @Inject
  private InjectionContext injectionContext;

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
  public Descriptor init(PluginContext context) throws Exception {
    return descriptor = new ControllersDescriptor(context.getClassLoader(), context.getConfig());
  }

  public InjectionContext getInjectionContext() {
    return injectionContext;
  }

  public List<RequestFilter> getFilters() {
    try {
      return getLifecycles(injectionContext);
    }
    catch (Exception e) {
      throw new UnsupportedOperationException("handle me cracefully", e);
    }
  }

  // This is done lazyly to avoid circular references issues
  private <B, I> ArrayList<RequestFilter> getLifecycles(InjectionContext<B, I> manager) throws Exception {
    if (filters == null) {
      ArrayList<RequestFilter> filters = new ArrayList<RequestFilter>();
      for (B lifeCycleBean : manager.resolveBeans(RequestFilter.class)) {
        I lifeCycleInstance = manager.create(lifeCycleBean);
        RequestFilter filter = (RequestFilter)manager.get(lifeCycleBean, lifeCycleInstance);
        filters.add(filter);
      }
      this.filters = filters;
    }
    return filters;
  }

  public void invoke(RequestBridge bridge) {
    Phase phase;
    if (bridge instanceof RenderBridge) {
      phase = Phase.VIEW;
    }
    else if (bridge instanceof ActionBridge) {
      phase = Phase.ACTION;
    }
    else if (bridge instanceof EventBridge) {
      phase = Phase.EVENT;
    }
    else if (bridge instanceof ResourceBridge) {
      phase = Phase.RESOURCE;
    }
    else {
      throw new AssertionError();
    }

    //
    Map<String, String[]> parameters = bridge.getParameters();

    //
    MethodHandle handle = bridge.getTarget();
    Method method = descriptor.getMethodByHandle(handle);

    //
    if (method == null) {
      StringBuilder sb = new StringBuilder("handle me gracefully : no method could be resolved for " +
          "phase=").append(phase).append(" handle=").append(handle).append(" parameters={");
      int index = 0;
      for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
        if (index++ > 0) {
          sb.append(',');
        }
        sb.append(entry.getKey()).append('=').append(Arrays.asList(entry.getValue()));
      }
      sb.append("}");
      throw new UnsupportedOperationException(sb.toString());
    }

    //
    Request request = new Request(this, method, parameters, bridge);

    //
    ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
    try {
      ClassLoader classLoader = injectionContext.getClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader);
      ScopeController.begin(request);
      bridge.begin(request);
      request.invoke();
      Response response = request.getResponse();
      if (response != null) {
        try {
          bridge.setResponse(response);
        }
        catch (IOException e) {
          throw new UndeclaredIOException(e);
        }
      }
    }
    finally {
      bridge.end();
      ScopeController.end();
      Thread.currentThread().setContextClassLoader(oldCL);
    }
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
