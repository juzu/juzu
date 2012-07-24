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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public enum GroupType {

  CAPTURING_GROUP("(", ")"),

  NON_CAPTURING_GROUP("(?:", ")"),

  POSITIVE_LOOKAHEAD("(?=", ")"),

  NEGATIVE_LOOKAHEAD("(?!", ")"),

  POSITIVE_LOOKBEHIND("(?<=", ")"),

  NEGATIVE_LOOKBEHIND("(?<!", ")");

  /** . */
  private static final GroupType[] ALL = values();

  public static GroupType forPrefix(String s) {
    if (s == null) {
      throw new NullPointerException("No null prefix accepted");
    }
    // No need for a fast lookup, iteration on array will do well
    for (GroupType type : ALL) {
      if (type.open.equals(s)) {
        return type;
      }
    }
    return null;
  }

  /** . */
  private final String open;

  /** . */
  private final String close;

  GroupType(String open, String close) {
    this.open = open;
    this.close = close;
  }

  public String getOpen() {
    return open;
  }

  public String getClose() {
    return close;
  }
}
