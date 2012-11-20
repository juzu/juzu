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

package juzu;

import juzu.impl.common.MimeType;

import java.io.IOException;

/**
 * <p>The <code>Dispatch</code> object represents the dispatch to a controller method. It can be used for generating
 * URL or as a {@link Response.Update} objects. A dispatch object can be obtained from a {@link juzu.request.RequestContext}
 * object for building controller methods, however the best way to obtain a builder is to use a controller companion
 * that provides a type safe way for creating fully configured dispatch.</p>
 *
 * <p>Type safe <code>Dispatch</code> factory method are generated for each view, action or resource controller methods. The
 * signature of a dispatch factory is the same as the original method.</p>
 * <p/>
 * <code><pre>
 *    public void MyController {
 *
 *       &#064;Action
 *       public Response.Update myAction(String param) {
 *          return MyController_.myRender();
 *       }
 *
 *       &#064;View
 *       public void myRender() {
 *          String url = MyController_.myAction("hello");
 *       }
 *    }
 * </pre></code>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class Dispatch extends Response.Update {

  /** . */
  private PropertyMap properties;

  /** . */
  private MimeType mimeType;

  protected Dispatch() {
    this.properties = null;
    this.mimeType = null;
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

  /**
   * Set or clear a property of the URL.
   *
   * @param propertyType the property type
   * @param propertyValue the property value
   * @param <T> the property generic type
   * @return this URL builder
   * @throws IllegalArgumentException if the property is not valid
   */
  public <T> Dispatch setProperty(PropertyType<T> propertyType, T propertyValue) throws IllegalArgumentException {
    String invalid = checkPropertyValidity(propertyType, propertyValue);
    if (invalid != null) {
      throw new IllegalArgumentException(invalid);
    }
    if (properties == null) {
      properties = new PropertyMap();
    }
    properties.setValue(propertyType, propertyValue);
    return this;
  }

  /**
   * @param propertyType  the property type
   * @param propertyValue the property value
   * @param <T>           the property generic type
   * @return null when the property is valid, an error message otherwise
   */
  protected abstract <T> String checkPropertyValidity(PropertyType<T> propertyType, T propertyValue);

  public String toString() {
    try {
      StringBuilder builder = new StringBuilder();
      renderURL(properties, mimeType, builder);
      return builder.toString();
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }

  public abstract void renderURL(
      PropertyMap properties,
      MimeType mimeType,
      Appendable appendable) throws IOException;

}
