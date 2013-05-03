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

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.impl.common.MimeType;
import juzu.io.Encoding;

/**
 * <p>The <code>Dispatch</code> object represents the dispatch to a controller method. It can be used for generating
 * URL or as a {@link juzu.Response.View} objects. A dispatch object can be obtained from a {@link juzu.request.RequestContext}
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
 *       public Response.View myAction(String param) {
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

  /**
   * <p>Set a parameter on the URL that will be built by this builder. This method replaces the parameter with the
   * given name . A parameter value of <code>null</code> indicates that this parameter should be removed.</p>
   *
   * @param name  the parameter name
   * @param value the parameter value
   * @throws NullPointerException if the name parameter is null
   */
  Dispatch setParameter(String name, String value) throws NullPointerException;

  /**
   * <p>Set a parameter on the URL that will be built by this builder. This method replaces the parameter with the
   * given name . A parameter value of <code>null</code> indicates that this parameter should be removed.</p>
   *
   * @param encoding the character encoding
   * @param name  the parameter name
   * @param value the parameter value
   * @throws NullPointerException if the name parameter is null
   * @throws IllegalArgumentException if the encoding is not allowed
   */
  Dispatch setParameter(Encoding encoding, String name, String value) throws NullPointerException;

  /**
   * Set a parameter. This method replaces the parameter with the given name . An zero length parameter value indicates
   * that this parameter should be removed.
   * <p/>
   * The inserted value is cloned before its insertion in the map.
   *
   * @param name  the parameter name
   * @param value the parameter value
   * @throws NullPointerException     if the name parameter or the value parameter is null
   * @throws IllegalArgumentException if any component of the value is null
   */
  Dispatch setParameter(String name, String[] value) throws NullPointerException, IllegalArgumentException;

  /**
   * <p>Set a parameter on the URL that will be built by this builder. This method replaces the parameter with the
   * given name . A parameter value of <code>null</code> indicates that this parameter should be removed.</p>
   *
   * @param encoding the character encoding
   * @param name  the parameter name
   * @param value the parameter value
   * @throws NullPointerException if the name parameter is null
   * @throws IllegalArgumentException if the encoding is not allowed, if any component of the value is null
   */
  Dispatch setParameter(Encoding encoding, String name, String[] value) throws IllegalArgumentException, NullPointerException;

  String toString();

}
