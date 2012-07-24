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

package juzu.impl.common;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface ParameterMap extends Map<String, String[]> {

  ParameterMap EMPTY = new EmptyParameterMap();

  /**
   * <p>Set a parameter on the URL that will be built by this builder. This method replaces the parameter with the
   * given name . A parameter value of <code>null</code> indicates that this parameter should be removed.</p>
   *
   * @param name  the parameter name
   * @param value the parameter value
   * @throws NullPointerException if the name parameter is null
   */
  void setParameter(String name, String value) throws NullPointerException;

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
  void setParameter(String name, String[] value) throws NullPointerException, IllegalArgumentException;

  /**
   * Set all parameters contained in the map. This method replaces the parameter with the given name . A parameter
   * value of with a zero length value array indicates that this parameter should be removed.
   * <p/>
   * Inserted values are cloned.
   *
   * @param parameters the parameters
   * @throws NullPointerException     if the parameters argument is null
   * @throws IllegalArgumentException if any key, if any value in the map is null or contains a null element
   */
  void setParameters(Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException;

  /**
   * Redefines equals to implement equality on the String[] type.
   *
   * @see Map#equals(Object)
   */
  boolean equals(Object o);
}
