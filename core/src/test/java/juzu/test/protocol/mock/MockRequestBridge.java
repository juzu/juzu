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

package juzu.test.protocol.mock;

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.impl.bridge.Parameters;
import juzu.impl.request.ControlParameter;
import juzu.request.RequestParameter;
import juzu.request.ResponseParameter;
import juzu.impl.bridge.spi.DispatchBridge;
import juzu.impl.common.Logger;
import juzu.impl.common.MimeType;
import juzu.impl.common.MethodHandle;
import juzu.impl.plugin.application.ApplicationLifeCycle;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.request.Method;
import juzu.impl.inject.Scoped;
import juzu.impl.inject.ScopedContext;
import juzu.impl.request.Request;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.request.ApplicationContext;
import juzu.request.Phase;
import juzu.request.UserContext;
import juzu.test.AbstractTestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MockRequestBridge implements RequestBridge {

  /** . */
  protected final ApplicationLifeCycle<?, ?> application;

  /** . */
  protected final MockClient client;

  /** . */
  private final MethodHandle target;

  /** . */
  private final ScopedContext attributes;

  /** . */
  private final MockHttpContext httpContext;

  /** . */
  private final MockSecurityContext securityContext;

  /** . */
  private final MockWindowContext windowContext;

  /** . */
  private final List<Scoped> attributesHistory;

  /** . */
  private final Map<ControlParameter, Object> arguments;

  /** . */
  protected Map<String, RequestParameter> requestParameters;

  /** . */
  protected Response response;

  public MockRequestBridge(ApplicationLifeCycle<?, ?> application, MockClient client, MethodHandle target, Map<String, String[]> parameters) {

    //
    Map<String, RequestParameter> requestParameters = Collections.emptyMap();
    for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
      if (requestParameters.isEmpty()) {
        requestParameters = new HashMap<String, RequestParameter>();
      }
      RequestParameter.create(parameter).addTo(requestParameters);
    }

    //
    Method<?> descriptor = application.getPlugin(ControllerPlugin.class).getDescriptor().getMethodByHandle(target);
    Map<ControlParameter, Object> arguments = descriptor.getArguments(requestParameters);


    //
    this.application = application;
    this.client = client;
    this.target = target;
    this.attributes = new ScopedContext(Logger.SYSTEM);
    this.httpContext = new MockHttpContext();
    this.securityContext = new MockSecurityContext();
    this.windowContext = new MockWindowContext();
    this.attributesHistory = new ArrayList<Scoped>();
    this.arguments = arguments;
    this.requestParameters = requestParameters;
  }

  public Map<String, RequestParameter> getRequestParameters() {
    return requestParameters;
  }

  public Map<ControlParameter, Object> getArguments() {
    return arguments;
  }

  public List<Scoped> getAttributesHistory() {
    return attributesHistory;
  }

  public MethodHandle getTarget() {
    return target;
  }

  public <T> T getProperty(PropertyType<T> propertyType) {
    return null;
  }

  public Scoped getFlashValue(Object key) {
    return client.getFlashValue(key);
  }

  public void setFlashValue(Object key, Scoped value) {
    client.setFlashValue(key, value);
  }

  public Scoped getRequestValue(Object key) {
    return attributes.get(key);
  }

  public void setRequestValue(Object key, Scoped value) {
    if (value != null) {
      attributes.set(key, value);
    }
    else {
      attributes.set(key, null);
    }
  }

  public Scoped getSessionValue(Object key) {
    return client.getSession().get(key);
  }

  public void setSessionValue(Object key, Scoped value) {
    if (value != null) {
      client.getSession().set(key, value);
    }
    else {
      client.getSession().set(key, null);
    }
  }

  public void purgeSession() {
    throw new UnsupportedOperationException();
  }

  public Scoped getIdentityValue(Object key) {
    return null;
  }

  public void setIdentityValue(Object key, Scoped value) {
  }

  public MockSecurityContext getSecurityContext() {
    return securityContext;
  }

  public MockHttpContext getHttpContext() {
    return httpContext;
  }

  public MockWindowContext getWindowContext() {
    return windowContext;
  }

  public UserContext getUserContext() {
    return client;
  }

  public ApplicationContext getApplicationContext() {
    return client.application;
  }

  public void close() {
  }

  public String _checkPropertyValidity(Phase phase, PropertyType<?> propertyType, Object propertyValue) {
    if (propertyType == PropertyType.ESCAPE_XML) {
      // OK
      return null;
    }
    else {
      return "Unsupported property " + propertyType + " = " + propertyValue;
    }
  }

  public final DispatchBridge createDispatch(final Phase phase, final MethodHandle target, final Map<String, ResponseParameter> parameters) throws NullPointerException, IllegalArgumentException {
    return new DispatchBridge() {

      public MethodHandle getTarget() {
        return target;
      }

      public Map<String, ResponseParameter> getParameters() {
        return parameters;
      }

      public <T> String checkPropertyValidity(PropertyType<T> propertyType, T propertyValue) {
        return _checkPropertyValidity(phase, propertyType, propertyValue);
      }

      public void renderURL(PropertyMap properties, MimeType mimeType, Appendable appendable) throws IOException {
        //
        Method method = application.getPlugin(ControllerPlugin.class).getDescriptor().getMethodByHandle(target);

        //
        JSON props = new JSON();
        if (properties != null) {
          for (PropertyType<?> property : properties) {
            Object value = properties.getValue(property);
            String valid = _checkPropertyValidity(method.getPhase(), property, value);
            if (valid != null) {
              throw new IllegalArgumentException(valid);
            }
            else {
              props.set(property.getClass().getName(), value);
            }
          }
        }

        //
        HashMap<String, String[]> foo = new HashMap<String, String[]>();
        for (ResponseParameter parameter : parameters.values()) {
          foo.put(parameter.getName(), parameter.toArray());
        }

        //
        JSON url = new JSON();
        url.set("target", target.toString());
        url.map("parameters", foo);
        url.set("properties", props);

        //
        url.toString(appendable);
      }
    };
  }

  public void begin(Request request) {
  }

  public void end() {
    attributesHistory.addAll(Tools.list(attributes));
    attributes.close();
  }

  public void setResponse(Response response) throws IllegalStateException, IOException {
    this.response = response;
  }

  public <T extends Throwable> T assertFailure(Class<T> expected) {
    Response.Error error = AbstractTestCase.assertInstanceOf(Response.Error.class, response);
    return AbstractTestCase.assertInstanceOf(expected, error.getCause());
  }
}
