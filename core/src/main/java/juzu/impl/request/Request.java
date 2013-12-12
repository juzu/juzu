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

package juzu.impl.request;

import juzu.Response;
import juzu.Scope;
import juzu.asset.AssetLocation;
import juzu.impl.bridge.Parameters;
import juzu.impl.bridge.spi.DispatchBridge;
import juzu.impl.bridge.spi.ScopedContext;
import juzu.impl.common.Tools;
import juzu.impl.inject.ScopeController;
import juzu.impl.inject.Scoped;
import juzu.impl.inject.ScopingContext;
import juzu.impl.inject.spi.BeanLifeCycle;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.plugin.application.Application;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.plugin.controller.descriptor.ControllersDescriptor;
import juzu.io.UndeclaredIOException;
import juzu.request.ApplicationContext;
import juzu.request.ClientContext;
import juzu.request.Dispatch;
import juzu.request.HttpContext;
import juzu.request.Phase;
import juzu.request.RequestContext;
import juzu.request.RequestParameter;
import juzu.request.Result;
import juzu.request.SecurityContext;
import juzu.request.UserContext;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Request implements ScopingContext {

  /** . */
  private static final Object[] EMPTY = new Object[0];

  public static Request getCurrent() {
    ContextLifeCycle context = current.get();
    return context != null ? context.getRequest() : null;
  }

  /** The unique static thread local we should use. */
  static final ThreadLocal<ContextLifeCycle> current = new ThreadLocal<ContextLifeCycle>();

  /** . */
  final LinkedHashSet<ContextLifeCycle> contextLifeCycles = new LinkedHashSet<ContextLifeCycle>();

  /** The controller for this request. */
  BeanLifeCycle controllerLifeCycle = null;

  /** . */
  final RequestBridge bridge;

  /** . */
  private final ControllerPlugin controllerPlugin;

  /** . */
  private final Map<String, RequestParameter> parameters;

  /** . */
  private final Map<ControlParameter, Object> arguments;

  /** . */
  private final Method<?> method;

  /** The response. */
  private Result result;

  public Request(
    ControllerPlugin controllerPlugin,
    Method method,
    Map<String, RequestParameter> parameters,
    RequestBridge bridge) {

    // Make a copy of the original arguments provided by the bridge
    Map<ControlParameter, Object> arguments = new HashMap<ControlParameter, Object>(bridge.getArguments());

    //
    this.bridge = bridge;
    this.parameters = parameters;
    this.arguments = arguments;
    this.controllerPlugin = controllerPlugin;
    this.method = method;
  }

  public Application getApplication() {
    return controllerPlugin.getApplication();
  }

  public ClientContext getClientContext() {
    return bridge.getClientContext();
  }

  public HttpContext getHttpContext() {
    return getBridge().getHttpContext();
  }

  public SecurityContext getSecurityContext() {
    return getBridge().getSecurityContext();
  }

  public UserContext getUserContext() {
    return getBridge().getUserContext();
  }

  public ApplicationContext getApplicationContext() {
    return getBridge().getApplicationContext();
  }

  public Method<?> getMethod() {
    return method;
  }

  public Phase getPhase() {
    return method.getPhase();
  }

  public ScopeController getScopeController() {
    return controllerPlugin.getInjectionContext().getScopeController();
  }

  public RequestBridge getBridge() {
    return bridge;
  }

  public Result getResult() {
    return result;
  }

  public void setResult(Result result) {
    this.result = result;
  }

  public void setResponse(Response response) {
    if (response == null) {
      result = null;
    } else {
      result = response.result();
    }
  }

  public Map<String, RequestParameter> getParameters() {
    return parameters;
  }

  public Map<ControlParameter, Object> getArguments() {
    return arguments;
  }

  public void setArguments(Map<ControlParameter, Object> arguments) {
    this.arguments.clear();
    this.arguments.putAll(arguments);
  }

  public void setArgument(ControlParameter parameter, Object value) {
    this.arguments.put(parameter, value);
  }

  public final Scoped getContextualValue(Scope scope, Object key) {
    ScopedContext context = bridge.getScopedContext(scope, false);
    return context != null ? context.get(key) : null;
  }

  public final void setContextualValue(Scope scope, Object key, Scoped value) {
    if (value == null) {
      ScopedContext context = bridge.getScopedContext(scope, false);
      if (context != null) {
        context.set(key, null);
      }
    }
    else {
      bridge.getScopedContext(scope, true).set(key, value);
    }
  }

  public boolean isActive(Scope scope) {
    switch (scope) {
      case IDENTITY:
        return false;
      default:
        return true;
    }
  }

  /** . */
  private int index = 0;

  /** The main contextual for this request. */
  private ContextLifeCycle contextLifeCycle;

  public void invoke() {
    boolean set = current.get() == null;
    try {

      //
      if (set) {
        current.set(contextLifeCycle = new ContextLifeCycle(this));
        getScopeController().begin(this);
      }

      //
      List<RequestFilter> filters = Tools.list(controllerPlugin.getInjectionContext().resolveInstances(RequestFilter.class));

      //
      if (index >= 0 && index < filters.size()) {

        RequestFilter plugin = filters.get(index);
        try {
          index++;
          plugin.invoke(this);
        }
        finally {
          index--;
        }
      }
      else if (index == filters.size()) {

        // Dispatch request
        Response response = dispatch(this, controllerPlugin.getInjectionContext());
        if (response != null) {
          setResponse(response);
        }
      }
      else {
        throw new AssertionError();
      }
    }
    finally {
      if (set) {
        contextLifeCycle.endContextual();
        current.set(null);
      }
    }
  }

  public Executor getExecutor() {
    final Iterable<ExecutionFilter> filters = controllerPlugin.getInjectionContext().resolveInstances(ExecutionFilter.class);
    return new Executor() {
      public void execute(Runnable command) {
        for (ExecutionFilter filter : filters) {
          command = filter.onCommand(command);
        }
        Request.this.execute(command);
      }
    };
  }

  private void execute(final Runnable runnable) throws RejectedExecutionException {
    // Create a new context - we add it here to keep a reference
    final ContextLifeCycle contextLifeCycle = new ContextLifeCycle(this);
    contextLifeCycles.add(contextLifeCycle);

    // Our wrapper for cleanup
    Runnable wrapper = new Runnable() {
      public void run() {
        try {
          getScopeController().begin(Request.this);
          current.set(contextLifeCycle);
          runnable.run();
        }
        finally {
          current.set(null);
          contextLifeCycle.endContextual();
        }
      }
    };

    // In some case execute cannot honour the execution and we should do the cleanup
    // otherwise it will never occur
    boolean executed = false;
    try {
      bridge.execute(wrapper);
      executed = true;
    }
    finally {
      if (!executed) {
        contextLifeCycle.endContextual();
      }
    }
  }

  public ContextLifeCycle suspend() {

    //
    ContextLifeCycle lifeCycle = current.get();
    if (lifeCycle == null) {
      throw new IllegalStateException("No current active request");
    } else if (lifeCycle.getRequest() != this) {
      throw new IllegalStateException("Current request is not active");
    }

    //
    current.set(null);

    //
    return lifeCycle;
  }

  private <T> void tryInject(Request request, ContextualParameter parameter, Class<T> type, T instance) {
    if (instance != null && type.isAssignableFrom(parameter.getType())) {
      request.setArgument(parameter, instance);
    }
  }

  private <B, I> Response dispatch(Request request, InjectionContext<B, I> manager) {

    // Create context
    RequestContext context = new RequestContext(this, method);

    //
    for (ControlParameter parameter : method.getParameters()) {
      if (parameter instanceof ContextualParameter) {
        ContextualParameter contextualParameter = (ContextualParameter)parameter;
        tryInject(request, contextualParameter, RequestContext.class, context);
        tryInject(request, contextualParameter, HttpContext.class, request.getHttpContext());
        tryInject(request, contextualParameter, SecurityContext.class, request.getSecurityContext());
        tryInject(request, contextualParameter, ApplicationContext.class, request.getApplicationContext());
        tryInject(request, contextualParameter, UserContext.class, request.getUserContext());
        if (bridge.getPhase() == Phase.RESOURCE || bridge.getPhase() == Phase.ACTION) {
          tryInject(request, contextualParameter, ClientContext.class, request.getClientContext());
        }
      }
    }

    // Get arguments
    Object[] args = new Object[method.getParameters().size()];
    for (int i = 0;i < args.length;i++) {
      ControlParameter parameter = method.getParameters().get(i);
      args[i] = arguments.get(parameter);
    }

    //
    Class<?> type = context.getMethod().getType();

    //
    controllerLifeCycle = manager.get(type);

    //
    if (controllerLifeCycle != null) {

      // Get controller
      Object controller;
      try {
        controller = controllerLifeCycle.get();
      }
      catch (InvocationTargetException e) {
        context.setResponse(Response.error(Tools.safeCause(e)));
        controller = null;
      }

      //
      if (controller != null) {

        // Begin request callback
        if (controller instanceof juzu.request.RequestLifeCycle) {
          try {
            ((juzu.request.RequestLifeCycle)controller).beginRequest(context);
          }
          catch (Exception e) {
            context.setResponse(new Response.Error(e));
          }
        }

        // If we have no response yet
        if (context.getResponse() == null) {
          // We invoke method on controller
          try {
            Object ret = context.getMethod().getMethod().invoke(controller, args);
            if (ret instanceof Response) {
              // We should check that it matches....
              // btw we should try to enforce matching during compilation phase
              // @Action -> Response.Action
              // @View -> Response.Mime
              // as we can do it
              context.setResponse((Response)ret);
            }
          }
          catch (InvocationTargetException e) {
             context.setResponse(Response.error(e.getCause()));
          }
          catch (IllegalAccessException e) {
            throw new UnsupportedOperationException("hanle me gracefully", e);
          }

          // End request callback
          if (controller instanceof juzu.request.RequestLifeCycle) {
            try {
              ((juzu.request.RequestLifeCycle)controller).endRequest(context);
            }
            catch (Exception e) {
              context.setResponse(Response.error(e));
            }
          }
        }
      }
    }

    //
    return context.getResponse();
  }

  private Dispatch createDispatch(Method<?> method, DispatchBridge spi) {
    ControllersDescriptor desc = controllerPlugin.getDescriptor();
    Dispatch dispatch;
    if (method.getPhase() == Phase.ACTION) {
      dispatch = new Phase.Action.Dispatch(spi);
    } else if (method.getPhase() == Phase.VIEW) {
      dispatch = new Phase.View.Dispatch(spi);
      dispatch.escapeXML(desc.getEscapeXML());
    } else if (method.getPhase() == Phase.RESOURCE) {
      dispatch = new Phase.Resource.Dispatch(spi);
      dispatch.escapeXML(desc.getEscapeXML());
    } else {
      throw new AssertionError();
    }
    dispatch.escapeXML(desc.getEscapeXML());
    return dispatch;
  }

  public Dispatch createDispatch(Method<?> method, Object[] args) {
    Parameters parameters = new Parameters();
    method.setArgs(args, parameters);
    DispatchBridge spi = getBridge().createDispatch(method.getPhase(), method.getHandle(), parameters);
    return createDispatch(method, spi);
  }

  public Dispatch createDispatch(Method<?> method) {
    DispatchBridge spi = getBridge().createDispatch(method.getPhase(), method.getHandle(), new Parameters());
    return createDispatch(method, spi);
  }

  private static Dispatch safeCreateDispatch(Method<?> method, Object[] args) {
    ContextLifeCycle context = current.get();
    if (context != null) {
      return context.getRequest().createDispatch(method, args);
    } else {
      // Should we output some warning ?
      return null;
    }
  }

  public static Phase.Action.Dispatch createActionDispatch(Method<Phase.Action> method) {
    return (Phase.Action.Dispatch)safeCreateDispatch(method, EMPTY);
  }

  public static Phase.Action.Dispatch createActionDispatch(Method<Phase.Action> method, Object arg) {
    return (Phase.Action.Dispatch)safeCreateDispatch(method, new Object[]{arg});
  }

  public static Phase.Action.Dispatch createActionDispatch(Method<Phase.Action> method, Object[] args) {
    return (Phase.Action.Dispatch)safeCreateDispatch(method, args);
  }

  public static Phase.View.Dispatch createViewDispatch(Method<Phase.View> method) {
    return (Phase.View.Dispatch)safeCreateDispatch(method, EMPTY);
  }

  public static Phase.View.Dispatch createViewDispatch(Method<Phase.View> method, Object arg) {
    return (Phase.View.Dispatch)safeCreateDispatch(method, new Object[]{arg});
  }

  public static Phase.View.Dispatch createViewDispatch(Method<Phase.View> method, Object[] args) {
    return (Phase.View.Dispatch)safeCreateDispatch(method, args);
  }

  public static Phase.Resource.Dispatch createResourceDispatch(Method<Phase.Resource> method) {
    return (Phase.Resource.Dispatch)safeCreateDispatch(method, EMPTY);
  }

  public static Phase.Resource.Dispatch createResourceDispatch(Method<Phase.Resource> method, Object arg) {
    return (Phase.Resource.Dispatch)safeCreateDispatch(method, new Object[]{arg});
  }

  public static Phase.Resource.Dispatch createResourceDispatch(Method<Phase.Resource> method, Object[] args) {
    return (Phase.Resource.Dispatch)safeCreateDispatch(method, args);
  }

  public void renderAssetURL(AssetLocation location, String uri, Appendable appendable) throws NullPointerException, UnsupportedOperationException, UndeclaredIOException {
    try {
      getBridge().renderAssetURL(location, uri, appendable);
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }
}
