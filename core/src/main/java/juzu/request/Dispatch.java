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
import juzu.impl.common.MimeType;

/**
 * <p>The <code>Dispatch</code> object represents the dispatch to a controller method. It can be used for generating
 * URL or as a {@link juzu.Response.Update} objects. A dispatch object can be obtained from a {@link juzu.request.RequestContext}
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
public interface Dispatch  {


  Dispatch with(MimeType mimeType);

  Dispatch with(PropertyMap properties);

  Dispatch escapeXML(Boolean escapeXML);

  /**
   * Set or clear a property of the URL.
   *
   * @param propertyType the property type
   * @param propertyValue the property value
   * @param <T> the property generic type
   * @return this URL builder
   * @throws IllegalArgumentException if the property is not valid
   */
  <T> Dispatch setProperty(PropertyType<T> propertyType, T propertyValue) throws IllegalArgumentException;

  String toString();

}
