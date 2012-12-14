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

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.UndeclaredIOException;
import juzu.impl.bridge.spi.DispatchSPI;
import juzu.impl.common.MethodHandle;
import juzu.impl.common.MimeType;

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

      public Dispatch(DispatchSPI delegate) {
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

      @Override
      public String toString() {
        return delegate.toString();
      }
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

      public Dispatch(DispatchSPI delegate) {
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

      @Override
      public MethodHandle getTarget() {
        return delegate.spi.getTarget();
      }

      @Override
      public Map<String, String[]> getParameters() {
        return delegate.spi.getParameters();
      }

      @Override
      public String toString() {
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

      public Dispatch(DispatchSPI delegate) {
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

      @Override
      public String toString() {
        return delegate.toString();
      }
    }
  }

  /** Action phase. */
  public static final Action ACTION = new Action();

  /** View phase. */
  public static final View VIEW = new View();

  /** Resource phase. */
  public static final Resource RESOURCE = new Resource();

  /** . */
  private static final List<Phase> values = Collections.unmodifiableList(Arrays.asList(ACTION, VIEW, RESOURCE));

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
    private final DispatchSPI spi;

    private AbstractDispatch(DispatchSPI spi) {
      this.properties = null;
      this.mimeType = null;
      this.spi = spi;
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

    public <T> Dispatch setProperty(PropertyType<T> propertyType, T propertyValue) throws IllegalArgumentException {
      String invalid = spi.checkPropertyValidity(propertyType, propertyValue);
      if (invalid != null) {
        throw new IllegalArgumentException(invalid);
      }
      if (properties == null) {
        properties = new PropertyMap();
      }
      properties.setValue(propertyType, propertyValue);
      return this;
    }

    public String toString() {
      try {
        StringBuilder builder = new StringBuilder();
        spi.renderURL(properties, mimeType, builder);
        return builder.toString();
      }
      catch (IOException e) {
        throw new UndeclaredIOException(e);
      }
    }
  }
}
