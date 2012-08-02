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

package juzu.impl.plugin.controller.metamodel;

import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.common.Cardinality;
import juzu.impl.common.JSON;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ParameterMetaModel extends MetaModelObject {

  /** . */
  final String name;

  /** . */
  final Cardinality cardinality;

  /** . */
  final ElementHandle.Class type;

  /** . */
  final String declaredType;

  public ParameterMetaModel(String name, Cardinality cardinality, ElementHandle.Class type, String declaredType) {
    this.name = name;
    this.cardinality = cardinality;
    this.type = type;
    this.declaredType = declaredType;
  }

  public String getName() {
    return name;
  }

  public Cardinality getCardinality() {
    return cardinality;
  }

  public ElementHandle.Class getType() {
    return type;
  }

  @Override
  public JSON toJSON() {
    return new JSON().
      set("name", name).
      set("type", type).
      set("declaredType", declaredType).
      set("cardinality", cardinality);
  }
}
