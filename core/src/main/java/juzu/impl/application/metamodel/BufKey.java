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

package juzu.impl.application.metamodel;

import juzu.impl.common.FQN;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.common.QN;

import javax.lang.model.element.Element;
import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class BufKey implements Serializable {

  /** . */
  final QN pkg;

  /** . */
  final ElementHandle element;

  /** . */
  final FQN annotationFQN;

  BufKey(ProcessingContext env, Element element, FQN annotationFQN) {
    this.pkg = QN.parse(env.getPackageOf(element).getQualifiedName());
    this.element = ElementHandle.create(element);
    this.annotationFQN = annotationFQN;
  }

  @Override
  public int hashCode() {
    return element.hashCode() ^ annotationFQN.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof BufKey) {
      BufKey that = (BufKey)obj;
      return element.equals(that.element) && annotationFQN.equals(that.annotationFQN);
    }
    return false;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[element=" + element + ",annotation=" + annotationFQN + "]";
  }
}
