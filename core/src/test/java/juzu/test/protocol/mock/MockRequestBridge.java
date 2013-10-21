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
import juzu.Scope;
import juzu.asset.AssetLocation;
import juzu.impl.bridge.spi.servlet.ServletScopedContext;
import juzu.impl.common.Tools;
import juzu.impl.io.BinaryOutputStream;
import juzu.impl.io.BinaryStream;
import juzu.impl.request.ControlParameter;
import juzu.request.Result;
import juzu.io.Chunk;
import juzu.io.Stream;
import juzu.request.RequestParameter;
import juzu.request.ResponseParameter;
import juzu.impl.bridge.spi.DispatchBridge;
import juzu.impl.common.Logger;
import juzu.impl.common.MimeType;
import juzu.impl.common.MethodHandle;
import juzu.impl.runtime.ApplicationRuntime;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.request.Method;
import juzu.impl.inject.Scoped;
import juzu.impl.bridge.spi.ScopedContext;
import juzu.impl.request.Request;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.common.JSON;
import juzu.request.ApplicationContext;
import juzu.request.Phase;
import juzu.request.UserContext;
import juzu.test.AbstractTestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MockRequestBridge implements RequestBridge {

  /** . */
  protected final ApplicationRuntime<?, ?> application;

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
  protected Result result;

  /** . */
  protected ByteArrayOutputStream buffer = null;

  /** . */
  protected Charset charset = Tools.ISO_8859_1;

  /** . */
  protected String mimeType;

  /** . */
  protected String title;

  public MockRequestBridge(ApplicationRuntime<?, ?> application, MockClient client, MethodHandle target, Map<String, String[]> parameters) {

    //
    Map<String, RequestParameter> requestParameters = Collections.emptyMap();
    for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
      if (requestParameters.isEmpty()) {
        requestParameters = new HashMap<String, RequestParameter>();
      }
      RequestParameter.create(parameter).appendTo(requestParameters);
    }

    //
    Method<?> descriptor = application.resolveBean(ControllerPlugin.class).getDescriptor().getMethodByHandle(target);
    Map<ControlParameter, Object> arguments = descriptor.getArguments(requestParameters);

    //
    ServletScopedContext attributes = new ServletScopedContext(Logger.SYSTEM) {
      @Override
      public void close() {
        attributesHistory.addAll(Tools.list(MockRequestBridge.this.attributes));
        super.close();
      }
    };

    //
    this.application = application;
    this.client = client;
    this.target = target;
    this.attributes = attributes;
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

  public ScopedContext getScopedContext(Scope scope, boolean create) {
    ScopedContext context;
    switch (scope) {
      case REQUEST:
        context = attributes;
        break;
      case FLASH:
        context = client.getFlashContext(create);
        break;
      case SESSION:
        context = client.getSession();
        break;
      default:
        throw new UnsupportedOperationException("Unsupported scope " + scope);
    }
    return context;
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
        Method method = application.resolveBean(ControllerPlugin.class).getDescriptor().getMethodByHandle(target);

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

  public void execute(Runnable runnable) throws RejectedExecutionException {
    new Thread(runnable).start();
  }

  public void end() {
  }

  public void renderAssetURL(AssetLocation location, String uri, Appendable appendable) throws NullPointerException, UnsupportedOperationException, IOException {
    throw new IllegalStateException();
  }

  public void setResult(Result result) throws IllegalArgumentException, IOException {
    if (result instanceof Result.Status) {
      Result.Status body = (Result.Status)result;
      body.streamable.send(new Stream() {
        BinaryStream dataStream = null;
        public void provide(Chunk chunk) {
          if (chunk instanceof Chunk.Property) {
            Chunk.Property property = (Chunk.Property)chunk;
            if (property.type == PropertyType.ENCODING) {
              charset = (Charset)property.value;
            } else if (property.type == PropertyType.MIME_TYPE) {
              mimeType = (String)property.value;
            } else if (property.type == PropertyType.TITLE) {
              title = (String)property.value;
            }
          } else if (chunk instanceof Chunk.Data) {
            Chunk.Data data = (Chunk.Data)chunk;
            if (dataStream == null) {
              dataStream = new BinaryOutputStream(charset, buffer = new ByteArrayOutputStream());
            }
            dataStream.provide(data);
          }
        }
        public void close(Thread.UncaughtExceptionHandler errorHandler) {
          dataStream.close(errorHandler);
        }
      });
    }
    this.result = result;
  }

  public <T extends Throwable> T assertFailure(Class<T> expected) {
    Result.Error error = AbstractTestCase.assertInstanceOf(Result.Error.class, result);
    return AbstractTestCase.assertInstanceOf(expected, error.cause);
  }
}
