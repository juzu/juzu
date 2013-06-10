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
import juzu.impl.bridge.Parameters;
import juzu.impl.bridge.spi.DispatchBridge;
import juzu.impl.bridge.spi.EventBridge;
import juzu.impl.bridge.spi.ScopedContext;
import juzu.impl.common.Tools;
import juzu.impl.inject.ScopeController;
import juzu.impl.inject.Scoped;
import juzu.impl.inject.ScopingContext;
import juzu.impl.inject.spi.BeanLifeCycle;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.bridge.spi.ActionBridge;
import juzu.impl.bridge.spi.RenderBridge;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.bridge.spi.ResourceBridge;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.plugin.controller.descriptor.ControllersDescriptor;
import juzu.request.ActionContext;
import juzu.request.Dispatch;
import juzu.request.EventContext;
import juzu.request.Phase;
import juzu.request.RenderContext;
import juzu.request.RequestContext;
import juzu.request.RequestParameter;
import juzu.request.ResourceContext;

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
  final RequestContext context;

  /** . */
  private final ControllerPlugin controllerPlugin;

  /** . */
  private final Map<String, RequestParameter> parameters;

  /** . */
  private final Map<ControlParameter, Object> arguments;

  /** The response. */
  private Response response;

  public Request(
    ControllerPlugin controllerPlugin,
    Method method,
    Map<String, RequestParameter> parameters,
    RequestBridge bridge) {
    RequestContext context;

    // Make a copy of the original arguments provided by the bridge
    Map<ControlParameter, Object> arguments = new HashMap<ControlParameter, Object>(bridge.getArguments());

    //
    if (bridge instanceof RenderBridge) {
      context = new RenderContext(this, method, (RenderBridge)bridge);
    }
    else if (bridge instanceof ActionBridge) {
      context = new ActionContext(this, method, (ActionBridge)bridge);
    }
    else if (bridge instanceof EventBridge) {
      context = new EventContext(this, method, (EventBridge)bridge);
    }
    else {
      context = new ResourceContext(this, method, (ResourceBridge)bridge);
    }

    //
    this.context = context;
    this.bridge = bridge;
    this.parameters = parameters;
    this.arguments = arguments;
    this.controllerPlugin = controllerPlugin;
  }

  public ScopeController getScopeController() {
    return controllerPlugin.getInjectionContext().getScopeController();
  }

  public RequestBridge getBridge() {
    return bridge;
  }

  public Response getResponse() {
    return response;
  }

  public void setResponse(Response response) {
    this.response = response;
  }

  public Map<String, RequestParameter> getParameters() {
    return parameters;
  }

  public RequestContext getContext() {
    return context;
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

        // Get arguments
        Method<?> method = context.getMethod();
        Object[] args = new Object[method.getParameters().size()];
        for (int i = 0;i < args.length;i++) {
          ControlParameter parameter = method.getParameters().get(i);
          args[i] = arguments.get(parameter);
        }

        // Dispatch request
        dispatch(this, args, controllerPlugin.getInjectionContext());
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

  private <B, I> void dispatch(Request request, Object[] args, InjectionContext<B, I> manager) {
    RequestContext context = request.getContext();
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
        request.response = Response.error(Tools.safeCause(e));
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
            request.response = new Response.Error(e);
          }
        }

        // If we have no response yet
        if (request.getResponse() == null) {
          // We invoke method on controller
          try {
            Object ret = context.getMethod().getMethod().invoke(controller, args);
            if (ret instanceof Response) {
              // We should check that it matches....
              // btw we should try to enforce matching during compilation phase
              // @Action -> Response.Action
              // @View -> Response.Mime
              // as we can do it
              request.response = (Response)ret;
            }
          }
          catch (InvocationTargetException e) {
            request.response = Response.error(e.getCause());
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
              request.response = Response.error(e);
            }
          }
        }
      }
    }
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
}
