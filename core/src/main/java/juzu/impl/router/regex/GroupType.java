/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
