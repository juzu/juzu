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
import juzu.impl.common.RunMode;
import juzu.impl.inject.ScopeController;
import juzu.impl.inject.Scoped;
import juzu.impl.inject.ScopingContext;
import juzu.impl.inject.spi.BeanLifeCycle;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.plugin.application.Application;
import juzu.impl.plugin.controller.ControllerService;
import juzu.impl.plugin.controller.descriptor.ControllersDescriptor;
import juzu.impl.value.ValueType;
import juzu.io.UndeclaredIOException;
import juzu.request.ApplicationContext;
import juzu.request.ClientContext;
import juzu.request.Dispatch;
import juzu.request.HttpContext;
import juzu.request.Phase;
import juzu.request.RequestParameter;
import juzu.request.SecurityContext;
import juzu.request.UserContext;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
  final ControllerService controllerPlugin;

  /** . */
  final ControllerHandler<?> handler;

  /** . */
  private Map<String, RequestParameter> parameterArguments;

  /** . */
  private Map<ContextualParameter, Object> contextualArguments;

  public Request(
    ControllerService controllerPlugin,
    ControllerHandler handler,
    RequestBridge bridge) {

    //
    this.bridge = bridge;
    this.controllerPlugin = controllerPlugin;
    this.handler = handler;
    this.parameterArguments = new HashMap<String, RequestParameter>();
    this.contextualArguments = new HashMap<ContextualParameter, Object>();
  }

  public Map<String, RequestParameter> getParameterArguments() {
    return parameterArguments;
  }

  public Map<ContextualParameter, Object> getContextualArguments() {
    return contextualArguments;
  }

  public RunMode getRunMode() {
    return bridge.getRunMode();
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

  public ControllerHandler<?> getHandler() {
    return handler;
  }

  public Phase getPhase() {
    return handler.getPhase();
  }

  public ScopeController getScopeController() {
    return controllerPlugin.getInjectionContext().getScopeController();
  }

  public RequestBridge getBridge() {
    return bridge;
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
    return true;
  }

  /** The main contextual for this request. */
  private ContextLifeCycle contextLifeCycle;

  public Response invoke() {
    boolean set = current.get() == null;
    try {

      //
      if (set) {
        current.set(contextLifeCycle = new ContextLifeCycle(this));
        getScopeController().begin(this);
      }

      // Create first stage
      Stage stage = new Stage.Unmarshalling(this);

      // Dispatch request
      return stage.invoke();
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

  private Dispatch createDispatch(ControllerHandler<?> handler, DispatchBridge spi) {
    ControllersDescriptor desc = controllerPlugin.getDescriptor();
    Dispatch dispatch;
    if (handler.getPhase() == Phase.ACTION) {
      dispatch = new Phase.Action.Dispatch(spi);
    } else if (handler.getPhase() == Phase.VIEW) {
      dispatch = new Phase.View.Dispatch(spi);
      dispatch.escapeXML(desc.getEscapeXML());
    } else if (handler.getPhase() == Phase.RESOURCE) {
      dispatch = new Phase.Resource.Dispatch(spi);
      dispatch.escapeXML(desc.getEscapeXML());
    } else {
      throw new AssertionError();
    }
    dispatch.escapeXML(desc.getEscapeXML());
    return dispatch;
  }

  private String valueOf(AnnotatedElement annotated, Object o) {
    ValueType vt = controllerPlugin.resolveValueType(o.getClass());
    if (vt != null) {
      return vt.format(annotated, o);
    } else {
      return null;
    }
  }

  private void setArgs(Object[] args, Parameters parameterMap, final ControllerHandler<?> handler) {
    int index = 0;
    for (ControlParameter parameter : handler.getParameters()) {
      if (parameter instanceof PhaseParameter) {
        PhaseParameter phaseParameter = (PhaseParameter)parameter;
        final int at = index++;
        Object value = args[at];
        if (value != null) {
          String name = phaseParameter.getMappedName();
          switch (phaseParameter.getCardinality()) {
            case SINGLE: {
              parameterMap.setParameter(name, valueOf(phaseParameter.getAnnotations(), value));
              break;
            }
            case ARRAY: {
              int length = Array.getLength(value);
              String[] array = new String[length];
              for (int i = 0;i < length;i++) {
                Object component = Array.get(value, i);
                array[i] = valueOf(phaseParameter.getAnnotations(), component);
              }
              parameterMap.setParameter(name, array);
              break;
            }
            case LIST: {
              Collection<?> c = (Collection<?>)value;
              int length = c.size();
              String[] array = new String[length];
              Iterator<?> iterator = c.iterator();
              for (int i = 0;i < length;i++) {
                Object element = iterator.next();
                array[i] = valueOf(phaseParameter.getAnnotations(), element);
              }
              parameterMap.setParameter(name, array);
              break;
            }
            default:
              throw new UnsupportedOperationException("Not yet implemented");
          }
        }
      } else if (parameter instanceof BeanParameter) {
        BeanParameter beanParameter = (BeanParameter)parameter;
        Object value = args[index++];
        Map<String, String[]> p = beanParameter.buildBeanParameter(controllerPlugin, handler.requiresPrefix, beanParameter.getName(), value);
        parameterMap.setParameters(p);
      }
    }
  }

  public Dispatch createDispatch(ControllerHandler<?> handler, Object[] args) {
    Parameters parameters = new Parameters();
    setArgs(args, parameters, handler);
    DispatchBridge spi = getBridge().createDispatch(handler.getPhase(), handler.getHandle(), parameters);
    return createDispatch(handler, spi);
  }

  public Dispatch createDispatch(ControllerHandler<?> handler) {
    DispatchBridge spi = getBridge().createDispatch(handler.getPhase(), handler.getHandle(), new Parameters());
    return createDispatch(handler, spi);
  }

  private static Dispatch safeCreateDispatch(ControllerHandler<?> handler, Object[] args) {
    ContextLifeCycle context = current.get();
    if (context != null) {
      return context.getRequest().createDispatch(handler, args);
    } else {
      // Should we output some warning ?
      return null;
    }
  }

  public static Phase.Action.Dispatch createActionDispatch(ControllerHandler<Phase.Action> handler) {
    return (Phase.Action.Dispatch)safeCreateDispatch(handler, EMPTY);
  }

  public static Phase.Action.Dispatch createActionDispatch(ControllerHandler<Phase.Action> handler, Object arg) {
    return (Phase.Action.Dispatch)safeCreateDispatch(handler, new Object[]{arg});
  }

  public static Phase.Action.Dispatch createActionDispatch(ControllerHandler<Phase.Action> handler, Object[] args) {
    return (Phase.Action.Dispatch)safeCreateDispatch(handler, args);
  }

  public static Phase.View.Dispatch createViewDispatch(ControllerHandler<Phase.View> handler) {
    return (Phase.View.Dispatch)safeCreateDispatch(handler, EMPTY);
  }

  public static Phase.View.Dispatch createViewDispatch(ControllerHandler<Phase.View> handler, Object arg) {
    return (Phase.View.Dispatch)safeCreateDispatch(handler, new Object[]{arg});
  }

  public static Phase.View.Dispatch createViewDispatch(ControllerHandler<Phase.View> handler, Object[] args) {
    return (Phase.View.Dispatch)safeCreateDispatch(handler, args);
  }

  public static Phase.Resource.Dispatch createResourceDispatch(ControllerHandler<Phase.Resource> handler) {
    return (Phase.Resource.Dispatch)safeCreateDispatch(handler, EMPTY);
  }

  public static Phase.Resource.Dispatch createResourceDispatch(ControllerHandler<Phase.Resource> handler, Object arg) {
    return (Phase.Resource.Dispatch)safeCreateDispatch(handler, new Object[]{arg});
  }

  public static Phase.Resource.Dispatch createResourceDispatch(ControllerHandler<Phase.Resource> handler, Object[] args) {
    return (Phase.Resource.Dispatch)safeCreateDispatch(handler, args);
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
