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

import java.util.Map;

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

  Map<String, String[]> getParameters();

  String toString();

}
