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

import juzu.impl.router.regex.RE;
import juzu.impl.common.QualifiedName;

import java.util.HashMap;
import java.util.Map;

/**
 * The render context used to compute the rendering of a parameter map.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class RenderContext {

  class Parameter {

    /** . */
    private final QualifiedName name;

    /** . */
    private String value;

    /** . */
    private final int index;

    /** This value is valid only if the parameter is matched. */
    private String match;

    private Parameter(QualifiedName name, String value, int index) {
      this.name = name;
      this.value = value;
      this.index = index;
    }

    public QualifiedName getName() {
      return name;
    }

    public String getValue() {
      return value;
    }

    public String getMatch() {
      return isMatched() ? match : null;
    }

    public boolean isMatched() {
      return getParam() != null;
    }

    public Param getParam() {
      return stack.getDepth() > 0 ? stack.get(index) : null;
    }

    public void remove(Param param, String match) {
      if (stack.getDepth() < 1) {
        throw new IllegalStateException();
      }
      if (stack.get(index) != null) {
        throw new AssertionError("We should not do that twice, shouldn't we ?");
      }
      stack.set(index, param);
      this.match = match;
    }
  }

  /** . */
  private final Map<QualifiedName, Parameter> parameters;

  /** . */
  private BitStack<Param> stack = new BitStack<Param>();

  /** . */
  RE.Matcher[] matchers;

  public RenderContext(Map<QualifiedName, String> map) {
    this();

    //
    reset(map);
  }

  public RenderContext() {
    this.parameters = new HashMap<QualifiedName, Parameter>();
    this.stack = new BitStack<Param>();
    this.matchers = null;
  }

  RE.Matcher matcher(RERef regex) {
    RE.Matcher matcher = matchers[regex.index];
    if (matcher == null) {
      matcher = matchers[regex.index] = regex.re.matcher();
    }
    return matcher;
  }

  /**
   * Reuse the context with new parameters.
   *
   * @param map the map
   */
  public void reset(Map<QualifiedName, String> map) {
    this.parameters.clear();
    this.stack.reset();

    //
    for (Map.Entry<QualifiedName, String> entry : map.entrySet()) {
      addParameter(entry.getKey(), entry.getValue());
    }
  }

  void addParameter(QualifiedName name, String value) {
    if (stack.getDepth() > 0) {
      throw new IllegalStateException();
    }
    if (name == null) {
      throw new NullPointerException();
    }
    if (value == null) {
      throw new NullPointerException();
    }
    Parameter parameter = parameters.get(name);
    if (parameter == null) {
      parameter = new Parameter(name, value, parameters.size());
      parameters.put(name, parameter);
    }
    else {
      parameter.value = value;
    }
  }

  Parameter getParameter(QualifiedName name) {
    return parameters.get(name);
  }

  Iterable<QualifiedName> getNames() {
    return parameters.keySet();
  }

  boolean isEmpty() {
    return stack.getDepth() == 0 || stack.isEmpty();
  }

  void enter() {
    if (stack.getDepth() == 0) {
      stack.init(parameters.size());
    }
    stack.push();
  }

  void leave() {
    stack.pop();
  }
}
