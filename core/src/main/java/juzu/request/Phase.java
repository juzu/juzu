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

import juzu.Consumes;
import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.UndeclaredIOException;
import juzu.impl.bridge.Parameters;
import juzu.impl.bridge.spi.DispatchBridge;
import juzu.impl.common.MethodHandle;
import juzu.impl.common.MimeType;
import juzu.io.Encoding;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A phase.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class Phase implements Serializable {

  /**
   * Ensure singleton.
   *
   * @return the resolved object
   */
  protected final Object readResolve() {
    if (this instanceof Action) {
      return ACTION;
    } else if (this instanceof View) {
      return VIEW;
    } else if (this instanceof Resource) {
      return RESOURCE;
    } else if (this instanceof Event) {
      return EVENT;
    } else {
      return this;
    }
  }

  public static final class Action extends Phase {

    public Action() {
      super(juzu.Action.class);
    }

    @Override
    public String name() {
      return "ACTION";
    }

    @Override
    public String id(Annotation annotation) throws ClassCastException {
      return ((juzu.Action)annotation).id();
    }

    public static class Dispatch implements juzu.request.Dispatch {

      /** . */
      private final AbstractDispatch delegate;

      public Dispatch(DispatchBridge delegate) {
        this.delegate = new AbstractDispatch(delegate);
      }

      public juzu.request.Dispatch with(MimeType mimeType) {
        delegate.with(mimeType);
        return this;
      }

      public juzu.request.Dispatch with(PropertyMap properties) {
        delegate.with(properties);
        return this;
      }

      public juzu.request.Dispatch escapeXML(Boolean escapeXML) {
        delegate.escapeXML(escapeXML);
        return this;
      }

      public <T> juzu.request.Dispatch setProperty(PropertyType<T> propertyType, T propertyValue) throws IllegalArgumentException {
        delegate.setProperty(propertyType, propertyValue);
        return this;
      }

      public final Dispatch setParameter(String name, String value) throws NullPointerException {
        delegate.setParameter(name, value);
        return this;
      }

      public final Dispatch setParameter(Encoding encoding, String name, String value) throws NullPointerException {
        delegate.setParameter(encoding, name, value);
        return this;
      }

      public final Dispatch setParameter(String name, String[] value) throws NullPointerException, IllegalArgumentException {
        delegate.setParameter(name, value);
        return this;
      }

      public final Dispatch setParameter(Encoding encoding, String name, String[] value) throws NullPointerException {
        delegate.setParameter(encoding, name, value);
        return this;
      }

      public final Dispatch setParameters(Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException {
        delegate.setParameters(parameters);
        return this;
      }

      public final Dispatch setParameters(Encoding encoding, Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException {
        delegate.setParameters(encoding, parameters);
        return this;
      }

      @Override
      public String toString() {
        return delegate.toString();
      }
    }
  }

  public static final class Event extends Phase {

    public Event() {
      super(Consumes.class);
    }

    @Override
    public String name() {
      return "EVENT";
    }
    @Override
    public String id(Annotation annotation) throws ClassCastException {
      throw new UnsupportedOperationException();
    }
  }

  public static final class View extends Phase {

    public View() {
      super(juzu.View.class);
    }

    @Override
    public String name() {
      return "VIEW";
    }

    @Override
    public String id(Annotation annotation) throws ClassCastException {
      return ((juzu.View)annotation).id();
    }

    public static class Dispatch extends Response.View implements juzu.request.Dispatch {

      /** . */
      private final AbstractDispatch delegate;

      public Dispatch(DispatchBridge delegate) {
        this.delegate = new AbstractDispatch(delegate);
      }

      public juzu.request.Dispatch with(MimeType mimeType) {
        delegate.with(mimeType);
        return this;
      }

      public juzu.request.Dispatch with(PropertyMap properties) {
        delegate.with(properties);
        return this;
      }

      public juzu.request.Dispatch escapeXML(Boolean escapeXML) {
        delegate.escapeXML(escapeXML);
        return this;
      }

      public <T> juzu.request.Dispatch setProperty(PropertyType<T> propertyType, T propertyValue) throws IllegalArgumentException {
        delegate.setProperty(propertyType, propertyValue);
        return this;
      }

      public Parameters getParameters() {
        return delegate.getParameters();
      }

      public MethodHandle getTarget() {
        return delegate.bridge.getTarget();
      }

      public final Dispatch setParameter(String name, String value) throws NullPointerException {
        delegate.setParameter(name, value);
        return this;
      }

      public final Dispatch setParameter(Encoding encoding, String name, String value) throws NullPointerException {
        delegate.setParameter(encoding, name, value);
        return this;
      }

      public final Dispatch setParameter(String name, String[] value) throws NullPointerException, IllegalArgumentException {
        delegate.setParameter(name, value);
        return this;
      }

      public final Dispatch setParameter(Encoding encoding, String name, String[] value) throws NullPointerException {
        delegate.setParameter(encoding, name, value);
        return this;
      }

      public final Dispatch setParameters(Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException {
        delegate.setParameters(parameters);
        return this;
      }

      public final Dispatch setParameters(Encoding encoding, Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException {
        delegate.setParameters(encoding, parameters);
        return this;
      }

      public boolean equals(Object obj) {
        if (obj == this) {
          return true;
        }
        if (obj instanceof Dispatch) {
          Dispatch that = (Dispatch)obj;
          return getParameters().equals(that.getParameters()) && properties.equals(that.properties);
        }
        return false;
      }

      @Override
      public final String toString() {
        return delegate.toString();
      }
    }
  }

  public static final class Resource extends Phase {

    public Resource() {
      super(juzu.Resource.class);
    }

    @Override
    public String name() {
      return "RESOURCE";
    }
    @Override
    public String id(Annotation annotation) throws ClassCastException {
      return ((juzu.Resource)annotation).id();
    }

    public static class Dispatch implements juzu.request.Dispatch {

      /** . */
      private final AbstractDispatch delegate;

      public Dispatch(DispatchBridge delegate) {
        this.delegate = new AbstractDispatch(delegate);
      }

      public final juzu.request.Dispatch with(MimeType mimeType) {
        delegate.with(mimeType);
        return this;
      }

      public final juzu.request.Dispatch with(PropertyMap properties) {
        delegate.with(properties);
        return this;
      }

      public final juzu.request.Dispatch escapeXML(Boolean escapeXML) {
        delegate.escapeXML(escapeXML);
        return this;
      }

      public final <T> juzu.request.Dispatch setProperty(PropertyType<T> propertyType, T propertyValue) throws IllegalArgumentException {
        delegate.setProperty(propertyType, propertyValue);
        return this;
      }

      public final Dispatch setParameter(String name, String value) throws NullPointerException {
        delegate.setParameter(name, value);
        return this;
      }

      public final Dispatch setParameter(Encoding encoding, String name, String value) throws NullPointerException {
        delegate.setParameter(encoding, name, value);
        return this;
      }

      public final Dispatch setParameter(String name, String[] value) throws NullPointerException, IllegalArgumentException {
        delegate.setParameter(name, value);
        return this;
      }

      public final Dispatch setParameter(Encoding encoding, String name, String[] value) throws NullPointerException {
        delegate.setParameter(encoding, name, value);
        return this;
      }

      public final Dispatch setParameters(Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException {
        delegate.setParameters(parameters);
        return this;
      }

      public final Dispatch setParameters(Encoding Encoding, Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException {
        delegate.setParameters(Encoding, parameters);
        return this;
      }

      @Override
      public final String toString() {
        return delegate.toString();
      }
    }
  }

  /** Action phase. */
  public static final Action ACTION = new Action();

  /** Action phase. */
  public static final Event EVENT = new Event();

  /** View phase. */
  public static final View VIEW = new View();

  /** Resource phase. */
  public static final Resource RESOURCE = new Resource();

  /** . */
  private static final List<Phase> values = Collections.unmodifiableList(Arrays.asList(ACTION, EVENT, VIEW, RESOURCE));

  public static List<Phase> values() {
    return values;
  }

  public static Phase valueOf(String s) {
    if ("ACTION".equals(s)) {
      return ACTION;
    } else if ("VIEW".equals(s)) {
      return VIEW;
    } else if ("RESOURCE".equals(s)) {
      return RESOURCE;
    } else if ("EVENT".equals(s)) {
      return EVENT;
    } else {
      return null;
    }
  }


  /** . */
  public final Class<? extends Annotation> annotation;

  Phase(Class<? extends Annotation> annotation) {
    this.annotation = annotation;
  }

  public abstract String name();

  public abstract String id(Annotation annotation) throws ClassCastException;

  private static class AbstractDispatch implements Dispatch {

    /** . */
    private PropertyMap properties;

    /** . */
    private MimeType mimeType;

    /** . */
    private final DispatchBridge bridge;

    private AbstractDispatch(DispatchBridge bridge) {
      this.properties = null;
      this.mimeType = null;
      this.bridge = bridge;
    }

    public Dispatch with(MimeType mimeType) {
      this.mimeType = mimeType;
      return this;
    }

    public Dispatch with(PropertyMap properties) {
      this.properties = new PropertyMap(properties);
      return this;
    }

    public Dispatch escapeXML(Boolean escapeXML) {
      setProperty(PropertyType.ESCAPE_XML, escapeXML);
      return this;
    }

    public Dispatch setParameter(String name, String value) throws NullPointerException {
      bridge.getParameters().setParameter(name, value);
      return this;
    }

    public Dispatch setParameter(Encoding encoding, String name, String value) throws NullPointerException {
      bridge.getParameters().setParameter(encoding, name, value);
      return this;
    }

    public Dispatch setParameter(String name, String[] value) throws NullPointerException, IllegalArgumentException {
      bridge.getParameters().setParameter(name, value);
      return this;
    }

    public Dispatch setParameter(Encoding encoding, String name, String[] value) throws NullPointerException {
      bridge.getParameters().setParameter(encoding, name, value);
      return this;
    }

    public Dispatch setParameters(Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException {
      bridge.getParameters().setParameters(parameters);
      return this;
    }

    public Dispatch setParameters(Encoding encoding, Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException {
      bridge.getParameters().setParameters(encoding, parameters);
      return this;
    }

    public <T> Dispatch setProperty(PropertyType<T> propertyType, T propertyValue) throws IllegalArgumentException {
      String invalid = bridge.checkPropertyValidity(propertyType, propertyValue);
      if (invalid != null) {
        throw new IllegalArgumentException(invalid);
      }
      if (properties == null) {
        properties = new PropertyMap();
      }
      properties.setValue(propertyType, propertyValue);
      return this;
    }

    public Parameters getParameters() {
      return bridge.getParameters();
    }

    public String toString() {
      try {
        StringBuilder builder = new StringBuilder();
        bridge.renderURL(properties, mimeType, builder);
        return builder.toString();
      }
      catch (IOException e) {
        throw new UndeclaredIOException(e);
      }
    }
  }
}
