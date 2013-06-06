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

package juzu.impl.plugin.controller;

import juzu.Response;
import juzu.impl.plugin.PluginDescriptor;
import juzu.io.UndeclaredIOException;
import juzu.impl.bridge.spi.ActionBridge;
import juzu.impl.bridge.spi.EventBridge;
import juzu.impl.bridge.spi.RenderBridge;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.bridge.spi.ResourceBridge;
import juzu.impl.common.MethodHandle;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.plugin.PluginContext;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.plugin.controller.descriptor.ControllersDescriptor;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.ControlParameter;
import juzu.impl.request.Method;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.request.RequestParameter;
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
  public PluginDescriptor init(PluginContext context) throws Exception {
    return descriptor = new ControllersDescriptor(context.getClassLoader(), context.getConfig());
  }

  public InjectionContext<?, ?> getInjectionContext() {
    return injectionContext;
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
    Map<String, RequestParameter> parameters = bridge.getRequestParameters();

    //
    MethodHandle handle = bridge.getTarget();
    Method method = descriptor.getMethodByHandle(handle);

    //
    if (method == null) {
      StringBuilder sb = new StringBuilder("handle me gracefully : no method could be resolved for " +
          "phase=").append(phase).append(" handle=").append(handle).append(" parameters={");
      int index = 0;
      for (RequestParameter parameter : parameters.values()) {
        if (index++ > 0) {
          sb.append(',');
        }
        sb.append(parameter.getName()).append("=[");
        for (int i = 0;i < parameter.size();i++) {
          if (i > 0) {
            sb.append(',');
          }
          sb.append(parameter.get(i));
        }
        sb.append(']');
      }
      sb.append("}");
      throw new UnsupportedOperationException(sb.toString());
    }

    //
    Request request = new Request(this, method, parameters, bridge);

    //

    //
    ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
    try {
      ClassLoader classLoader = injectionContext.getClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader);
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
      Thread.currentThread().setContextClassLoader(oldCL);
    }
  }

  public void invoke(Request request) {
    // Inject RequestContext in the arguments
    RequestContext context = request.getContext();
    Method<?> method = context.getMethod();
    for (ControlParameter parameter : method.getParameters()) {
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
