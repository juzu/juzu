/*
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.impl.router;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class RequestParam extends Param {

  /** . */
  final QualifiedName name;

  /** . */
  final String matchName;

  /** . */
  final RERef matchPattern;

  /** . */
  final ControlMode controlMode;

  RequestParam(QualifiedName name, String matchName, RERef matchPattern, ControlMode controlMode) {
    super(name);

    //
    if (matchName == null) {
      throw new NullPointerException("No null match name accepted");
    }
    if (controlMode == null) {
      throw new NullPointerException("No null control mode accepted");
    }

    //
    this.name = name;
    this.matchName = matchName;
    this.matchPattern = matchPattern;
    this.controlMode = controlMode;
  }

  boolean matchValue(String value) {
    return matchPattern == null || matchPattern.re.matcher().matches(value);
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder extends AbstractBuilder {

    /** . */
    private String name;

    /** . */
    private String value;

    /** . */
    private boolean literal;

    /** . */
    private ControlMode controlMode;

    Builder() {
      this.value = null;
      this.controlMode = ControlMode.OPTIONAL;
      this.literal = true;
    }

    RequestParam build(Router router) {
      Builder descriptor = this;

      //
      RERef matchValue = null;
      if (descriptor.value != null) {
        PatternBuilder matchValueBuilder = new PatternBuilder();
        matchValueBuilder.expr("^");
        if (!descriptor.literal) {
          matchValueBuilder.expr(descriptor.value);
        }
        else {
          matchValueBuilder.literal(descriptor.value);
        }
        matchValueBuilder.expr("$");
        matchValue = router.compile(matchValueBuilder.build());
      }

      //
      return new RequestParam(
          descriptor.getQualifiedName(),
          descriptor.getName(),
          matchValue,
          descriptor.getControlMode());
    }

    Builder named(String name) {
      this.name = name;
      return this;
    }

    Builder required() {
      this.controlMode = ControlMode.REQUIRED;
      return this;
    }

    Builder optional() {
      this.controlMode = ControlMode.OPTIONAL;
      return this;
    }

    Builder matchByValue(String value) {
      this.value = value;
      this.literal = true;
      return this;
    }

    Builder matchByPattern(String pattern) {
      this.value = pattern;
      this.literal = false;
      return this;
    }

    String getName() {
      return name;
    }

    Builder setName(String name) {
      this.name = name;
      return this;
    }

    ControlMode getControlMode() {
      return controlMode;
    }

    void setControlMode(ControlMode controlMode) {
      this.controlMode = controlMode;
    }
  }
}
