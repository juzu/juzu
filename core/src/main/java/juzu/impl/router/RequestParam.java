/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package juzu.impl.router;

import juzu.impl.router.metadata.RequestParamDescriptor;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class RequestParam extends Param {

  static RequestParam create(RequestParamDescriptor descriptor, Router router) {
    if (descriptor == null) {
      throw new NullPointerException("No null descriptor accepted");
    }

    //
    Regex matchValue = null;
    if (descriptor.getValue() != null) {
      PatternBuilder matchValueBuilder = new PatternBuilder();
      matchValueBuilder.expr("^");
      if (descriptor.getValueType() == ValueType.PATTERN) {
        matchValueBuilder.expr(descriptor.getValue());
      }
      else {
        matchValueBuilder.literal(descriptor.getValue());
      }
      matchValueBuilder.expr("$");
      matchValue = router.compile(matchValueBuilder.build());
    }

    //
    return new RequestParam(
        descriptor.getQualifiedName(),
        descriptor.getName(),
        matchValue,
        descriptor.getControlMode(),
        descriptor.getValueMapping());
  }

  /** . */
  final QualifiedName name;

  /** . */
  final String matchName;

  /** . */
  final Regex matchPattern;

  /** . */
  final ControlMode controlMode;

  /** . */
  final ValueMapping valueMapping;

  RequestParam(QualifiedName name, String matchName, Regex matchPattern, ControlMode controlMode, ValueMapping valueMapping) {
    super(name);

    //
    if (matchName == null) {
      throw new NullPointerException("No null match name accepted");
    }
    if (controlMode == null) {
      throw new NullPointerException("No null control mode accepted");
    }
    if (valueMapping == null) {
      throw new NullPointerException("No null value mapping accepted");
    }

    //
    this.name = name;
    this.matchName = matchName;
    this.matchPattern = matchPattern;
    this.controlMode = controlMode;
    this.valueMapping = valueMapping;
  }

  boolean matchValue(String value) {
    return matchPattern == null || matchPattern.matcher().matches(value);
  }
}
