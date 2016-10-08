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
import juzu.impl.common.Tools;
import juzu.impl.plugin.ServiceContext;
import juzu.impl.plugin.ServiceDescriptor;
import juzu.impl.plugin.application.Application;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.ControlParameter;
import juzu.impl.request.ControllerHandler;
import juzu.impl.request.RequestFilter;
import juzu.impl.value.ValueType;
import juzu.request.Phase;
import juzu.io.UndeclaredIOException;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.common.MethodHandle;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.plugin.application.ApplicationService;
import juzu.impl.plugin.controller.descriptor.ControllersDescriptor;
import juzu.impl.request.Request;
import juzu.request.RequestParameter;

import javax.inject.Inject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerService extends ApplicationService {

  /** . */
  private ControllersDescriptor descriptor;

  /** . */
  final ArrayList<ValueType<?>> valueTypes = new ArrayList<ValueType<?>>();

  /** . */
  final List<RequestFilter<?>>                             filters = new ArrayList<RequestFilter<?>>();

  /** . */
  @Inject
  private Application application;

  public ControllerService() {
    super("controller");
  }

  public Application getApplication() {
    return application;
  }

  public ControllersDescriptor getDescriptor() {
    return descriptor;
  }

  public ControllerResolver<ControllerHandler> getResolver() {
    return descriptor != null ? descriptor.getResolver() : null;
  }

  @Override
  public ServiceDescriptor init(ServiceContext context) throws Exception {
    valueTypes.addAll(ValueType.DEFAULT);
    for (ValueType<?> valueType : Tools.loadService(ValueType.class, context.getClassLoader())) {
      valueTypes.add(valueType);
    }
    return descriptor = new ControllersDescriptor(context.getClassLoader(), context.getConfig());
  }

  public InjectionContext<?, ?> getInjectionContext() {
    return application.getInjectionContext();
  }
  
  public List<RequestFilter<?>> getFilters() {
    if (filters.isEmpty()) {
      synchronized (filters) {
        if (filters.isEmpty()) {
          // Build the filter list
          for (RequestFilter<?> filter : getInjectionContext().resolveInstances(RequestFilter.class)) {
            filters.add(filter);
          }
        }
      }
    }
    return filters;
  }

  public <T> ValueType<T> resolveValueType(Class<T> type) {
    for (int i = 0;i < valueTypes.size();i++) {
      ValueType<?> valueType = valueTypes.get(i);
      for (Class<?> tmp : valueType.getTypes()) {
        if (tmp.equals(type)) {
          return (ValueType<T>)valueType;
        }
      }
    }
    return null;
  }

  public void invoke(RequestBridge bridge) {

    //
    MethodHandle handle = bridge.getTarget();
    ControllerHandler<?> handler = descriptor.getMethodByHandle(handle);
    if (handler == null) {
      StringBuilder sb = new StringBuilder("handle me gracefully : no method could be resolved for " +
          "phase=").append(bridge.getPhase()).append(" handle=").append(handle).append(" parameters={");
      int index = 0;
      for (RequestParameter parameter : bridge.getRequestArguments().values()) {
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
    Request request = new Request(this, handler, bridge);

    //
    ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
    try {
      ClassLoader classLoader = application.getClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader);
      bridge.begin(request);

      //
      Response result = request.invoke();

      //
      if (result instanceof Response.Error && descriptor.getErrorController() != null) {
        Class<? extends juzu.Handler<Response.Error, Response>> a = descriptor.getErrorController();
        Method m;
        try {
          m = a.getMethod("handle", Response.Error.class);
        }
        catch (NoSuchMethodException e) {
          throw new UndeclaredThrowableException(e);
        }

        //
        ContextualParameter argument = new ContextualParameter("argument", Response.Error.class);
        handler = new ControllerHandler<Phase.View>(null, Phase.VIEW, a, m, Collections.<ControlParameter>singletonList(argument));
        request = new Request(this, handler, bridge);
        request.getContextualArguments().put(argument, result);
        result = request.invoke();
      }

      //
      if (result != null) {
        try {
          bridge.setResponse(result);
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
}
