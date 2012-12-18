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

package juzu.impl.plugin.template;

import juzu.Path;

import javax.enterprise.util.AnnotationLiteral;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PathLiteral extends AnnotationLiteral<Path> implements Path {

  /** . */
  private final String value;

  /**
   * Create a new path literal implementing the {@link juzu.Path} annotation interface.
   *
   * @param value the name value
   * @throws NullPointerException if the value is null
   */
  public PathLiteral(String value) throws NullPointerException {
    if (value == null) {
      throw new NullPointerException("No null value accepted");
    }
    this.value = value;
  }

  public String value() {
    return value;
  }
}
