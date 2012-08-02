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

package juzu.test.protocol.mock;

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.impl.plugin.application.ApplicationContext;
import juzu.impl.common.MethodHandle;
import juzu.impl.plugin.controller.descriptor.MethodDescriptor;
import juzu.impl.inject.Scoped;
import juzu.impl.inject.ScopedContext;
import juzu.impl.request.Request;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.request.Phase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MockRequestBridge implements RequestBridge {

  /** . */
  protected final ApplicationContext application;

  /** . */
  protected final MockClient client;

  /** . */
  private final MethodHandle target;

  /** . */
  private final Map<String, String[]> parameters;

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

  public MockRequestBridge(ApplicationContext application, MockClient client, MethodHandle target, Map<String, String[]> parameters) {
    this.application = application;
    this.client = client;
    this.target = target;
    this.parameters = parameters;
    this.attributes = new ScopedContext();
    this.httpContext = new MockHttpContext();
    this.securityContext = new MockSecurityContext();
    this.windowContext = new MockWindowContext();
    this.attributesHistory = new ArrayList<Scoped>();
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

  public Map<String, String[]> getParameters() {
    return parameters;
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

  public void close() {
  }

  public <T> String checkPropertyValidity(Phase phase, PropertyType<T> propertyType, T propertyValue) {
    return _checkPropertyValidity(phase, propertyType, propertyValue);
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

  public String renderURL(MethodHandle target, Map<String, String[]> parameters, PropertyMap properties) {

    //
    MethodDescriptor method = application.getDescriptor().getControllers().getMethodByHandle(target);

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
    JSON url = new JSON();
    url.set("target", target.toString());
    url.map("parameters", parameters);
    url.set("properties", props);
    return url.toString();
  }

  public void begin(Request request) {
  }

  public void end() {
    attributesHistory.addAll(Tools.list(attributes));
    attributes.close();
  }
}
