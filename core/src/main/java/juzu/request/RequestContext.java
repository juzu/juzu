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

package juzu.request;

import juzu.PropertyType;
import juzu.Response;
import juzu.impl.request.Method;
import juzu.impl.request.Request;
import juzu.impl.bridge.spi.RequestBridge;

import java.util.Map;
import java.util.concurrent.Executor;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class RequestContext {

  /** . */
  private static final Object[] EMPTY = new Object[0];

  /** . */
  protected final Method method;

  /** . */
  protected final Request request;

  public RequestContext(Request request, Method method) {
    this.request = request;
    this.method = method;
  }

  public Method getMethod() {
    return method;
  }

  public Map<String, RequestParameter> getParameters() {
    return request.getParameters();
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

  public <T> T getProperty(PropertyType<T> propertyType) {
    return getBridge().getProperty(propertyType);
  }

  /**
   * Provide an {@link Executor} the specified modes.
   *
   * @param contextual propagate the Juzu context or not
   * @param async execute in an another thread or not
   */
  public Executor getExecutor(boolean contextual, boolean async) {
    return request.getExecutor(contextual, async);
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
    return request.createDispatch(method);
  }

  public Phase.Action.Dispatch createActionDispatch(Method<Phase.Action> method) {
    return (Phase.Action.Dispatch)request.createDispatch(method, EMPTY);
  }

  public Phase.Action.Dispatch createActionDispatch(Method<Phase.Action> method, Object arg) {
    return (Phase.Action.Dispatch)request.createDispatch(method, new Object[]{arg});
  }

  public Phase.Action.Dispatch createActionDispatch(Method<Phase.Action> method, Object[] args) {
    return (Phase.Action.Dispatch)request.createDispatch(method, args);
  }

  public Phase.View.Dispatch createViewDispatch(Method<Phase.View> method) {
    return (Phase.View.Dispatch)request.createDispatch(method, EMPTY);
  }

  public Phase.View.Dispatch createViewDispatch(Method<Phase.View> method, Object arg) {
    return (Phase.View.Dispatch)request.createDispatch(method, new Object[]{arg});
  }

  public Phase.View.Dispatch createViewDispatch(Method<Phase.View> method, Object[] args) {
    return (Phase.View.Dispatch)request.createDispatch(method, args);
  }

  public Phase.Resource.Dispatch createResourceDispatch(Method<Phase.Resource> method) {
    return (Phase.Resource.Dispatch)request.createDispatch(method, EMPTY);
  }

  public Phase.Resource.Dispatch createResourceDispatch(Method<Phase.Resource> method, Object arg) {
    return (Phase.Resource.Dispatch)request.createDispatch(method, new Object[]{arg});
  }

  public Phase.Resource.Dispatch createResourceDispatch(Method<Phase.Resource> method, Object[] args) {
    return (Phase.Resource.Dispatch)request.createDispatch(method, args);
  }

  public Response getResponse() {
    return request.getResponse();
  }

  public void setResponse(Response response) {
    request.setResponse(response);
  }
}
