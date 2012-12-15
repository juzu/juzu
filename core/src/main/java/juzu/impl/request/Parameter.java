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

package juzu.impl.request;

/**
 * A parameter of a controller
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class Parameter {

  /** . */
  private final String name;

  /** . */
  private final Class<?> type;

  public Parameter(String name, Class<?> type) throws NullPointerException {
    if (name == null) {
      throw new NullPointerException("No null parameter name accepted");
    }

    //
    this.name = name;
    this.type = type;
  }

  /**
   * Returns the parameter name.
   *
   * @return the parameter name
   */
  public String getName() {
    return name;
  }

  public Class<?> getType() {
    return type;
  }

  public abstract Argument create(Object value);

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    else if (obj instanceof Parameter) {
      Parameter that = (Parameter)obj;
      return name.equals(that.name);
    }
    else {
      return false;
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[name=" + name + "]";
  }
}
