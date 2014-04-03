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
import juzu.impl.common.AbstractAnnotatedElement;
import juzu.impl.common.RunMode;
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
import juzu.impl.value.ValueType;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
  private final Map<String, RequestParameter> parameterArguments;

  /** . */
  private final Map<ContextualParameter, Object> contextualArguments;

  /** . */
  private final Handler<?> handler;

  /** The response. */
  private Result result;

  public Request(
    ControllerPlugin controllerPlugin,
    Handler handler,
    Map<String, RequestParameter> parameterArguments,
    Map<ContextualParameter, Object> contextualArguments,
    RequestBridge bridge) {

    //
    this.bridge = bridge;
    this.parameterArguments = parameterArguments;
    this.contextualArguments = contextualArguments;
    this.controllerPlugin = controllerPlugin;
    this.handler = handler;
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

  public Handler<?> getHandler() {
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

  public Map<String, RequestParameter> getParameterArguments() {
    return parameterArguments;
  }

  public Map<ContextualParameter, Object> getContextualArguments() {
    return contextualArguments;
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

  private <B, I> Response dispatch(Request request, InjectionContext<B, I> manager) {

    //
    Class<?> controllerType = handler.getType();
    controllerLifeCycle = manager.get(controllerType);
    if (controllerLifeCycle != null) {

      // Get controller
      Object controller;
      try {
        controller = controllerLifeCycle.get();
      }
      catch (InvocationTargetException e) {
        return Response.error(e.getCause());
      }

      // Create context
      RequestContext context = new RequestContext(this, handler);

      // Build arguments
      Object[] args = new Object[handler.getParameters().size()];
      final Annotation[][] annotations = handler.getMethod().getParameterAnnotations();
      for (int i = 0;i < args.length;i++) {
        ControlParameter parameter = handler.getParameters().get(i);
        Object value;
        if (parameter instanceof PhaseParameter) {
          PhaseParameter phaseParam = (PhaseParameter)parameter;
          RequestParameter requestParam = parameterArguments.get(phaseParam.getMappedName());
          if (requestParam != null) {
            ValueType<?> valueType = controllerPlugin.resolveValueType(phaseParam.getValueType());
            if (valueType != null) {
              List values = new ArrayList(requestParam.size());
              for (String s : requestParam) {
                Object converted;
                final int index = i;
                try {
                  AbstractAnnotatedElement annotated = new AbstractAnnotatedElement() {
                    @Override
                    public Annotation[] getDeclaredAnnotations() {
                      return annotations[index];
                    }
                  };
                  converted = valueType.parse(annotated, s);
                }
                catch (Exception e) {
                  return Response.error(e);
                }
                values.add(converted);
              }
              value = phaseParam.getValue(values);
            } else {
              value = null;
            }
          } else {
            value = null;
          }
          Class<?> type = phaseParam.getType();
          if (value == null && type.isPrimitive()) {
            if (type == int.class) {
              value = 0;
            } else if (type == long.class) {
              value = 0L;
            } else if (type == byte.class) {
              value = (byte)0;
            } else if (type == short.class) {
              value = (short)0;
            } else if (type == boolean.class) {
              value = false;
            } else if (type == float.class) {
              value = 0.0f;
            } else if (type == double.class) {
              value = 0.0d;
            } else if (type == char.class) {
              value = '\u0000';
            }
          }
        } else if (parameter instanceof BeanParameter) {
          BeanParameter beanParam = (BeanParameter)parameter;
          Class<?> type = beanParam.getType();
          try {
            value = beanParam.createMappedBean(controllerPlugin, handler.requiresPrefix, type, beanParam.getName(), parameterArguments);
          }
          catch (Exception e) {
            value = null;
          }
        } else {
          ContextualParameter contextualParameter = (ContextualParameter)parameter;
          value = contextualArguments.get(contextualParameter);
          if (value == null) {
            Class<?> contextualType = contextualParameter.getType();
            if (RequestContext.class.isAssignableFrom(contextualType)) {
              value = context;
            } else if (HttpContext.class.isAssignableFrom(contextualType)) {
              value = request.getHttpContext();
            } else if (SecurityContext.class.isAssignableFrom(contextualType)) {
              value = request.getSecurityContext();
            } else if (ApplicationContext.class.isAssignableFrom(contextualType)) {
              value = request.getApplicationContext();
            } else if (UserContext.class.isAssignableFrom(contextualType)) {
              value = request.getUserContext();
            } else if (ClientContext.class.isAssignableFrom(contextualType) && (bridge.getPhase() == Phase.RESOURCE || bridge.getPhase() == Phase.ACTION)) {
              value = request.getClientContext();
            }
          }
        }
        args[i] = value;
      }

      // Begin request callback
      if (controller instanceof juzu.request.RequestLifeCycle) {
        try {
          ((juzu.request.RequestLifeCycle)controller).beginRequest(context);
        }
        catch (Exception e) {
          return new Response.Error(e);
        }
      }

      // If we have no response yet
      if (context.getResponse() == null) {
        // We invoke method on controller
        try {
          Object ret = context.getHandler().getMethod().invoke(controller, args);
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

      //
      return context.getResponse();
    } else {
      // Handle that...
      return null;
    }
  }

  private Dispatch createDispatch(Handler<?> handler, DispatchBridge spi) {
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

  private void setArgs(Object[] args, Parameters parameterMap, final Handler<?> handler) {
    int index = 0;
    for (ControlParameter parameter : handler.getParameters()) {
      if (parameter instanceof PhaseParameter) {
        PhaseParameter phaseParameter = (PhaseParameter)parameter;
        final int at = index++;
        Object value = args[at];
        AnnotatedElement annotated = new AbstractAnnotatedElement() {
          @Override
          public Annotation[] getDeclaredAnnotations() {
            return handler.getMethod().getParameterAnnotations()[at];
          }
        };
        if (value != null) {
          String name = phaseParameter.getMappedName();
          switch (phaseParameter.getCardinality()) {
            case SINGLE: {
              parameterMap.setParameter(name, valueOf(annotated, value));
              break;
            }
            case ARRAY: {
              int length = Array.getLength(value);
              String[] array = new String[length];
              for (int i = 0;i < length;i++) {
                Object component = Array.get(value, i);
                array[i] = valueOf(annotated, component);
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
                array[i] = valueOf(annotated, element);
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

  public Dispatch createDispatch(Handler<?> handler, Object[] args) {
    Parameters parameters = new Parameters();
    setArgs(args, parameters, handler);
    DispatchBridge spi = getBridge().createDispatch(handler.getPhase(), handler.getHandle(), parameters);
    return createDispatch(handler, spi);
  }

  public Dispatch createDispatch(Handler<?> handler) {
    DispatchBridge spi = getBridge().createDispatch(handler.getPhase(), handler.getHandle(), new Parameters());
    return createDispatch(handler, spi);
  }

  private static Dispatch safeCreateDispatch(Handler<?> handler, Object[] args) {
    ContextLifeCycle context = current.get();
    if (context != null) {
      return context.getRequest().createDispatch(handler, args);
    } else {
      // Should we output some warning ?
      return null;
    }
  }

  public static Phase.Action.Dispatch createActionDispatch(Handler<Phase.Action> handler) {
    return (Phase.Action.Dispatch)safeCreateDispatch(handler, EMPTY);
  }

  public static Phase.Action.Dispatch createActionDispatch(Handler<Phase.Action> handler, Object arg) {
    return (Phase.Action.Dispatch)safeCreateDispatch(handler, new Object[]{arg});
  }

  public static Phase.Action.Dispatch createActionDispatch(Handler<Phase.Action> handler, Object[] args) {
    return (Phase.Action.Dispatch)safeCreateDispatch(handler, args);
  }

  public static Phase.View.Dispatch createViewDispatch(Handler<Phase.View> handler) {
    return (Phase.View.Dispatch)safeCreateDispatch(handler, EMPTY);
  }

  public static Phase.View.Dispatch createViewDispatch(Handler<Phase.View> handler, Object arg) {
    return (Phase.View.Dispatch)safeCreateDispatch(handler, new Object[]{arg});
  }

  public static Phase.View.Dispatch createViewDispatch(Handler<Phase.View> handler, Object[] args) {
    return (Phase.View.Dispatch)safeCreateDispatch(handler, args);
  }

  public static Phase.Resource.Dispatch createResourceDispatch(Handler<Phase.Resource> handler) {
    return (Phase.Resource.Dispatch)safeCreateDispatch(handler, EMPTY);
  }

  public static Phase.Resource.Dispatch createResourceDispatch(Handler<Phase.Resource> handler, Object arg) {
    return (Phase.Resource.Dispatch)safeCreateDispatch(handler, new Object[]{arg});
  }

  public static Phase.Resource.Dispatch createResourceDispatch(Handler<Phase.Resource> handler, Object[] args) {
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
