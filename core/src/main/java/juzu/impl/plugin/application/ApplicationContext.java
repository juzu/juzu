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

package juzu.impl.plugin.application;

import juzu.Response;
import juzu.UndeclaredIOException;
import juzu.impl.common.MethodHandle;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.plugin.controller.descriptor.MethodDescriptor;
import juzu.impl.inject.Export;
import juzu.impl.inject.ScopeController;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.impl.bridge.spi.ActionBridge;
import juzu.impl.bridge.spi.RenderBridge;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.bridge.spi.ResourceBridge;
import juzu.request.Phase;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Export
@Singleton
public class ApplicationContext {

  /** . */
  private final ApplicationDescriptor descriptor;

  /** . */
  final InjectionContext<?, ?> injectManager;

  /** . */
  private final ControllerPlugin controller;

  /** . */
  public ArrayList<RequestFilter> lifecycles;

  @Inject
  public ApplicationContext(InjectionContext injectManager, ApplicationDescriptor descriptor, ControllerPlugin controller) throws Exception {
    this.descriptor = descriptor;
    this.injectManager = injectManager;
    this.controller = controller;
  }

  // This is done lazyly to avoid circular references issues
  private <B, I> ArrayList<RequestFilter> getLifecycles(InjectionContext<B, I> manager) throws Exception {
    if (lifecycles == null) {
      ArrayList<RequestFilter> lifeCycles = new ArrayList<RequestFilter>();
      for (B lifeCycleBean : manager.resolveBeans(RequestFilter.class)) {
        I lifeCycleInstance = manager.create(lifeCycleBean);
        RequestFilter lifeCycle = (RequestFilter)manager.get(lifeCycleBean, lifeCycleInstance);
        lifeCycles.add(lifeCycle);
      }
      lifecycles = lifeCycles;
    }
    return lifecycles;
  }

  public String getName() {
    return descriptor.getName();
  }

  public List<RequestFilter> getLifecycles() {
    try {
      return getLifecycles(injectManager);
    }
    catch (Exception e) {
      throw new UnsupportedOperationException("handle me cracefully", e);
    }
  }

  public ClassLoader getClassLoader() {
    return injectManager.getClassLoader();
  }

  public ApplicationDescriptor getDescriptor() {
    return descriptor;
  }

  public InjectionContext getInjectManager() {
    return injectManager;
  }

  public void invoke(RequestBridge bridge) throws ApplicationException {
    Phase phase;
    if (bridge instanceof RenderBridge) {
      phase = Phase.VIEW;
    }
    else if (bridge instanceof ActionBridge) {
      phase = Phase.ACTION;
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
    MethodDescriptor method = controller.getDescriptor().getMethodByHandle(handle);

    //
    if (method == null) {
      StringBuilder sb = new StringBuilder("handle me gracefully : no method could be resolved for " +
        "phase=" + phase + " and parameters={");
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
    Object[] args = method.getArgs(parameters);
    Request request = new Request(this, method, parameters, args, bridge);

    //
    ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
    try {
      ClassLoader classLoader = injectManager.getClassLoader();
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

  public Object resolveBean(String name) throws ApplicationException {
    return resolveBean(injectManager, name);
  }

  private <B, I> Object resolveBean(InjectionContext<B, I> manager, String name) throws ApplicationException {
    B bean = manager.resolveBean(name);
    if (bean != null) {
      try {
        I cc = manager.create(bean);
        return manager.get(bean, cc);
      }
      catch (InvocationTargetException e) {
        throw new ApplicationException(e.getCause());
      }
    }
    else {
      return null;
    }
  }
}
