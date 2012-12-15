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

package juzu.request;

import juzu.PropertyType;
import juzu.impl.bridge.spi.DispatchSPI;
import juzu.impl.common.ParameterHashMap;
import juzu.impl.common.ParameterMap;
import juzu.impl.plugin.application.ApplicationContext;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.request.Method;
import juzu.impl.request.Request;
import juzu.impl.bridge.spi.RequestBridge;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class RequestContext {

  /** . */
  private static final Object[] EMPTY = new Object[0];

  /** . */
  protected final ApplicationContext application;

  /** . */
  protected final Method method;

  /** . */
  protected final Request request;

  public RequestContext(Request request, ApplicationContext application, Method method) {
    this.request = request;
    this.application = application;
    this.method = method;
  }

  public ApplicationContext getApplication() {
    return application;
  }

  public Method getMethod() {
    return method;
  }

  public Map<String, String[]> getParameters() {
    return request.getParameters();
  }

  public HttpContext getHttpContext() {
    return getBridge().getHttpContext();
  }

  public SecurityContext getSecurityContext() {
    return getBridge().getSecurityContext();
  }

  public <T> T getProperty(PropertyType<T> propertyType) {
    return getBridge().getProperty(propertyType);
  }

  public abstract Phase getPhase();

  protected abstract RequestBridge getBridge();

  /**
   * Create a dispatch object with unset parameters.
   *
   * @param method the method descriptor
   * @return the corresponding dispatch object
   */
  public Dispatch createDispatch(Method<?> method) {
    DispatchSPI spi = getBridge().createDispatch(method.getPhase(), method.getHandle(), ParameterMap.EMPTY);
    return createDispatch(method, spi);
  }

  public Phase.Action.Dispatch createActionDispatch(Method<Phase.Action> method) {
    return (Phase.Action.Dispatch)createDispatch(method, EMPTY, ParameterMap.EMPTY);
  }

  public Phase.Action.Dispatch createActionDispatch(Method<Phase.Action> method, Object arg) {
    return (Phase.Action.Dispatch)createDispatch(method, new Object[]{arg}, new ParameterHashMap());
  }

  public Phase.Action.Dispatch createActionDispatch(Method<Phase.Action> method, Object[] args) {
    return (Phase.Action.Dispatch)createDispatch(method, args, new ParameterHashMap());
  }

  public Phase.View.Dispatch createViewDispatch(Method<Phase.View> method) {
    return (Phase.View.Dispatch)createDispatch(method, EMPTY, ParameterMap.EMPTY);
  }

  public Phase.View.Dispatch createViewDispatch(Method<Phase.View> method, Object arg) {
    return (Phase.View.Dispatch)createDispatch(method, new Object[]{arg}, new ParameterHashMap());
  }

  public Phase.View.Dispatch createViewDispatch(Method<Phase.View> method, Object[] args) {
    return (Phase.View.Dispatch)createDispatch(method, args, new ParameterHashMap());
  }

  public Phase.Resource.Dispatch createResourceDispatch(Method<Phase.Resource> method) {
    return (Phase.Resource.Dispatch)createDispatch(method, EMPTY, ParameterMap.EMPTY);
  }

  public Phase.Resource.Dispatch createResourceDispatch(Method<Phase.Resource> method, Object arg) {
    return (Phase.Resource.Dispatch)createDispatch(method, new Object[]{arg}, new ParameterHashMap());
  }

  public Phase.Resource.Dispatch createResourceDispatch(Method<Phase.Resource> method, Object[] args) {
    return (Phase.Resource.Dispatch)createDispatch(method, args, new ParameterHashMap());
  }

  private Dispatch createDispatch(Method<?> method, Object[] args, ParameterMap parameters) {
    method.setArgs(args, parameters);
    DispatchSPI spi = getBridge().createDispatch(method.getPhase(), method.getHandle(), parameters);
    return createDispatch(method, spi);
  }

  private Dispatch createDispatch(Method<?> method, DispatchSPI spi) {
    ApplicationDescriptor desc = application.getDescriptor();
    Dispatch dispatch;
    if (method.getPhase() == Phase.ACTION) {
      dispatch = new Phase.Action.Dispatch(spi);
    } else if (method.getPhase() == Phase.VIEW) {
      dispatch = new Phase.View.Dispatch(spi);
      dispatch.escapeXML(desc.getControllers().getEscapeXML());
    } else if (method.getPhase() == Phase.RESOURCE) {
      dispatch = new Phase.Resource.Dispatch(spi);
      dispatch.escapeXML(desc.getControllers().getEscapeXML());
    } else {
      throw new AssertionError();
    }
    dispatch.escapeXML(desc.getControllers().getEscapeXML());
    return dispatch;
  }
}
