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

package juzu.impl.router.regex;

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Quantifier {

  public enum Mode {

    GREEDY(""), RELUCTANT("?"), POSSESSIVE("+");

    /** . */
    private final String value;

    Mode(String value) {
      this.value = value;
    }
  }

  public static class Range {

    /** . */
    private final int min;

    /** . */
    private final Integer max;

    public Range(int min, Integer max) {
      this.min = min;
      this.max = max;
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      else if (o instanceof Range) {
        Range that = (Range)o;
        return min == that.min && (max == null ? that.max == null : max.equals(that.max));
      }
      return false;
    }

    public static Range onceOrNotAtAll() {
      return new Range(0, 1);
    }

    public static Range zeroOrMore() {
      return new Range(0, null);
    }

    public static Range oneOrMore() {
      return new Range(1, null);
    }

    public static Range exactly(int value) {
      return new Range(value, value);
    }

    public static Range atLeast(int value) {
      return new Range(value, null);
    }

    public static Range between(int min, int max) {
      return new Range(min, max);
    }
  }

  /** . */
  private final Mode mode;

  /** . */
  private final Range range;

  public Quantifier(Mode mode, int min, Integer max) {
    if (mode == null) {
      throw new NullPointerException("No null mode accepted");
    }

    //
    this.mode = mode;
    this.range = new Range(min, max);
  }

  public Quantifier(Mode mode, Range range) {
    if (mode == null) {
      throw new NullPointerException("No null mode accepted");
    }
    if (range == null) {
      throw new NullPointerException("No null range accepted");
    }

    //
    this.mode = mode;
    this.range = range;
  }

  public int getMin() {
    return range.min;
  }

  public static Quantifier onceOrNotAtAll(Mode mode) {
    return new Quantifier(mode, 0, 1);
  }

  public static Quantifier zeroOrMore(Mode mode) {
    return new Quantifier(mode, 0, null);
  }

  public static Quantifier oneOrMore(Mode mode) {
    return new Quantifier(mode, 1, null);
  }

  public static Quantifier exactly(Mode mode, int value) {
    return new Quantifier(mode, value, value);
  }

  public static Quantifier atLeast(Mode mode, int value) {
    return new Quantifier(mode, value, null);
  }

  public static Quantifier between(Mode mode, int min, int max) {
    return new Quantifier(mode, min, max);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    else if (o instanceof Quantifier) {
      Quantifier that = (Quantifier)o;
      return mode == that.mode && range.equals(that.range);
    }
    return false;
  }


  @Override
  public String toString() {
    try {
      StringBuilder sb = new StringBuilder();
      toString(sb);
      return sb.toString();
    }
    catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  public void toString(Appendable appendable) throws IOException {
    if (range.min == 0) {
      if (range.max == null) {
        appendable.append('*').append(mode.value);
      }
      else if (range.max == 1) {
        appendable.append('?').append(mode.value);
      }
    }
    else if (range.min == 1 && range.max == null) {
      appendable.append('+').append(mode.value);
    }
    else if (range.max == null) {
      appendable.append('{').append(Integer.toString(range.min)).append(",").append('}').append(mode.value);
    }
    else if (range.min == range.max) {
      appendable.append('{').append(Integer.toString(range.min)).append('}').append(mode.value);
    }
    else {
      appendable.append('{').append(Integer.toString(range.min)).append(",").append(range.max.toString()).append('}').append(mode.value);
    }
  }
}