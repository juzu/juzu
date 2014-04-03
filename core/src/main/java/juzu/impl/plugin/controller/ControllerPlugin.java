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

import juzu.impl.common.Spliterator;
import juzu.impl.common.Tools;
import juzu.impl.plugin.PluginDescriptor;
import juzu.impl.plugin.application.Application;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.ControlParameter;
import juzu.impl.request.EntityUnmarshaller;
import juzu.impl.request.Handler;
import juzu.impl.value.ValueType;
import juzu.request.ClientContext;
import juzu.request.Result;
import juzu.io.UndeclaredIOException;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.common.MethodHandle;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.plugin.PluginContext;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.plugin.controller.descriptor.ControllersDescriptor;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.request.RequestParameter;
import juzu.request.Phase;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerPlugin extends ApplicationPlugin implements RequestFilter {

  /** . */
  private ControllersDescriptor descriptor;

  /** . */
  final ArrayList<ValueType<?>> valueTypes = new ArrayList<ValueType<?>>();

  /** . */
  @Inject
  private Application application;

  public ControllerPlugin() {
    super("controller");
  }

  public Application getApplication() {
    return application;
  }

  public ControllersDescriptor getDescriptor() {
    return descriptor;
  }

  public ControllerResolver<Handler> getResolver() {
    return descriptor != null ? descriptor.getResolver() : null;
  }

  @Override
  public PluginDescriptor init(PluginContext context) throws Exception {
    valueTypes.addAll(ValueType.DEFAULT);
    for (ValueType<?> valueType : Tools.loadService(ValueType.class, context.getClassLoader())) {
      valueTypes.add(valueType);
    }
    return descriptor = new ControllersDescriptor(context.getClassLoader(), context.getConfig());
  }

  public InjectionContext<?, ?> getInjectionContext() {
    return application.getInjectionContext();
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
    Phase phase = bridge.getPhase();

    //
    Map<String, RequestParameter> parameterArguments = new HashMap<String, RequestParameter>(bridge.getRequestArguments());
    MethodHandle handle = bridge.getTarget();
    Handler<?> handler = descriptor.getMethodByHandle(handle);

    //
    if (handler == null) {
      StringBuilder sb = new StringBuilder("handle me gracefully : no method could be resolved for " +
          "phase=").append(phase).append(" handle=").append(handle).append(" parameters={");
      int index = 0;
      for (RequestParameter parameter : parameterArguments.values()) {
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

    // Make a copy of the original arguments provided by the bridge
    Map<ContextualParameter, Object> contextualArguments = new HashMap<ContextualParameter, Object>();
    for (ControlParameter a : handler.getParameters()) {
      if (a instanceof ContextualParameter) {
        contextualArguments.put((ContextualParameter)a, null);
      }
    }
    contextualArguments.putAll(bridge.getContextualArguments(contextualArguments.keySet()));

    //
    ClientContext clientContext = bridge.getClientContext();
    if (clientContext != null) {
      String contentType = clientContext.getContentType();
      if (contentType != null) {
        Spliterator i = new Spliterator(contentType, ';');

        //
        String mediaType;
        if (i.hasNext()) {
          mediaType = i.next().trim();

          //
          if (!mediaType.equals("application/x-www-form-urlencoded")) {
            for (EntityUnmarshaller reader : Tools.loadService(EntityUnmarshaller.class, application.getClassLoader())) {
              try {
                if (reader.accept(mediaType)) {
                  reader.unmarshall(mediaType, clientContext, contextualArguments.entrySet(), parameterArguments);
                  break;
                }
              }
              catch (IOException e) {
                throw new UnsupportedOperationException("handle me gracefully", e);
              }
            }
          }
        }
      }
    }

    //
    Request request = new Request(this, handler, parameterArguments, contextualArguments, bridge);

    //
    ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
    try {
      ClassLoader classLoader = application.getClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader);
      bridge.begin(request);


      request.invoke();
      Result result = request.getResult();
      if (result != null) {
        try {
          bridge.setResult(result);
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
    request.invoke();
  }
}
