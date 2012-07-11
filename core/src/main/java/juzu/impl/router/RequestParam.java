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

  static Builder builder() {
    return new Builder();
  }

  static class Builder extends AbstractBuilder {

    /** . */
    private String name;

    /** . */
    private String value;

    /** . */
    private ValueType valueType;

    /** . */
    private ControlMode controlMode;

    /** . */
    private ValueMapping valueMapping;

    Builder() {
      this.value = null;
      this.controlMode = ControlMode.OPTIONAL;
      this.valueType = ValueType.LITERAL;
      this.valueMapping = ValueMapping.CANONICAL;
    }

    RequestParam build(Router router) {
      Builder descriptor = this;

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

    Builder named(String name) {
      this.name = name;
      return this;
    }

    Builder matchedByLiteral(String value) {
      this.value = value;
      this.valueType = ValueType.LITERAL;
      return this;
    }

    Builder matchedByPattern(String value) {
      this.value = value;
      this.valueType = ValueType.PATTERN;
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

    Builder neverEmpty() {
      this.valueMapping = ValueMapping.NEVER_EMPTY;
      return this;
    }

    Builder neverNull() {
      this.valueMapping = ValueMapping.NEVER_NULL;
      return this;
    }

    Builder canonical() {
      this.valueMapping = ValueMapping.CANONICAL;
      return this;
    }

    String getName() {
      return name;
    }

    Builder setName(String name) {
      this.name = name;
      return this;
    }

    String getValue() {
      return value;
    }

    void setValue(String value) {
      this.value = value;
    }

    ValueType getValueType() {
      return valueType;
    }

    void setValueType(ValueType valueType) {
      this.valueType = valueType;
    }

    ControlMode getControlMode() {
      return controlMode;
    }

    void setControlMode(ControlMode controlMode) {
      this.controlMode = controlMode;
    }

    ValueMapping getValueMapping() {
      return valueMapping;
    }

    void setValueMapping(ValueMapping valueMapping) {
      this.valueMapping = valueMapping;
    }
  }
}
