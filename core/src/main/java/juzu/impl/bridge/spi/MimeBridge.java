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

package juzu.impl.bridge.spi;

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.impl.common.MethodHandle;
import juzu.request.Phase;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface MimeBridge extends RequestBridge {

  /**
   * @param phase         the phase
   * @param propertyType  the property type
   * @param propertyValue the property value
   * @param <T>           the property generic type
   * @return null when the property is valid, an error message otherwise
   */
  <T> String checkPropertyValidity(Phase phase, PropertyType<T> propertyType, T propertyValue);

  /**
   * Renders an URL.
   *
   * @param target     the target
   * @param parameters the url parameters
   * @param properties the url properties
   * @return the rendered URL
   * @throws IllegalArgumentException if any argument is not valid
   */
  String renderURL(
      MethodHandle target,
      Map<String, String[]> parameters,
      PropertyMap properties) throws IllegalArgumentException;

}
