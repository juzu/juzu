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

package juzu.impl.router;

import juzu.impl.common.QualifiedName;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RequestParam extends Param {

  /** . */
  final String matchedName;

  /** . */
  final RERef matchPattern;

  /** . */
  final boolean required;

  RequestParam(QualifiedName name, String matchedName, RERef matchPattern, boolean required) {
    super(name);

    //
    if (matchedName == null) {
      throw new NullPointerException("No null match name accepted");
    }

    //
    this.matchedName = matchedName;
    this.matchPattern = matchPattern;
    this.required = required;
  }

  public String getMatchedName() {
    return matchedName;
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
    private boolean required;

    Builder() {
      this.value = null;
      this.required = false;
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
          required);
    }

    Builder named(String name) {
      this.name = name;
      return this;
    }

    Builder required() {
      this.required = true;
      return this;
    }

    Builder optional() {
      this.required = false;
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
  }
}
