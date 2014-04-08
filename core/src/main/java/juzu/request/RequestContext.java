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
import juzu.impl.request.ControllerHandler;
import juzu.impl.request.Request;
import juzu.impl.bridge.spi.RequestBridge;

import java.util.Map;
import java.util.concurrent.Executor;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RequestContext {

  /** . */
  private static final Object[] EMPTY = new Object[0];

  /** . */
  protected final ControllerHandler handler;

  /** . */
  protected final Request request;

  /** . */
  protected Response response;

  public RequestContext(Request request, ControllerHandler handler) {
    this.request = request;
    this.handler = handler;
  }

  public ControllerHandler getHandler() {
    return handler;
  }

  public Map<String, RequestParameter> getParameters() {
    return request.getParameterArguments();
  }

  public ClientContext getClientContext() {
    return getBridge().getClientContext();
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
   */
  public final Executor getExecutor() {
    return request.getExecutor();
  }

  public final Phase getPhase() {
    return getBridge().getPhase();
  }

  final RequestBridge getBridge() {
    return request.getBridge();
  }

  /**
   * Create a dispatch object with unset parameters.
   *
   * @param handler the method descriptor
   * @return the corresponding dispatch object
   */
  public Dispatch createDispatch(ControllerHandler<?> handler) {
    return request.createDispatch(handler);
  }

  public Phase.Action.Dispatch createActionDispatch(ControllerHandler<Phase.Action> handler) {
    return (Phase.Action.Dispatch)request.createDispatch(handler, EMPTY);
  }

  public Phase.Action.Dispatch createActionDispatch(ControllerHandler<Phase.Action> handler, Object arg) {
    return (Phase.Action.Dispatch)request.createDispatch(handler, new Object[]{arg});
  }

  public Phase.Action.Dispatch createActionDispatch(ControllerHandler<Phase.Action> handler, Object[] args) {
    return (Phase.Action.Dispatch)request.createDispatch(handler, args);
  }

  public Phase.View.Dispatch createViewDispatch(ControllerHandler<Phase.View> handler) {
    return (Phase.View.Dispatch)request.createDispatch(handler, EMPTY);
  }

  public Phase.View.Dispatch createViewDispatch(ControllerHandler<Phase.View> handler, Object arg) {
    return (Phase.View.Dispatch)request.createDispatch(handler, new Object[]{arg});
  }

  public Phase.View.Dispatch createViewDispatch(ControllerHandler<Phase.View> handler, Object[] args) {
    return (Phase.View.Dispatch)request.createDispatch(handler, args);
  }

  public Phase.Resource.Dispatch createResourceDispatch(ControllerHandler<Phase.Resource> handler) {
    return (Phase.Resource.Dispatch)request.createDispatch(handler, EMPTY);
  }

  public Phase.Resource.Dispatch createResourceDispatch(ControllerHandler<Phase.Resource> handler, Object arg) {
    return (Phase.Resource.Dispatch)request.createDispatch(handler, new Object[]{arg});
  }

  public Phase.Resource.Dispatch createResourceDispatch(ControllerHandler<Phase.Resource> handler, Object[] args) {
    return (Phase.Resource.Dispatch)request.createDispatch(handler, args);
  }

  public Response getResponse() {
    return response;
  }

  public void setResponse(Response response) {
    this.response = response;
  }
}
