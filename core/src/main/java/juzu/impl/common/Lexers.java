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

package juzu.impl.common;

import java.util.regex.Pattern;

/**
 * Gather various lexers here.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class Lexers {

  /** The name validator. */
  static final Pattern NAME_VALIDATOR = Pattern.compile("(?!\\.)" + "[^/]+" + "(?<!\\.)");

  /** . */
  static final int PARSE_CANONICAL = 0;

  /** . */
  static final int PARSE_ANY = 1;

  /**
   * Path parsing method and returns an array. The last two values of the array are the
   *
   * @param mode {@link #PARSE_CANONICAL} : rejects any '.' or '..' / {@link #PARSE_ANY} : accepts '.' or '..'
   * @param base the base path
   * @param padding the first index that will be written
   * @param path the path to parse
   * @param off the first char to parse
   * @return the parsed path as a String[]
   * @throws IllegalArgumentException if the path is not valid
   */
  static String[] parsePath(int mode, String[] base, int padding, String path, int off) throws IllegalArgumentException {
    return parsePath(mode, base, padding, path, off, 0);
  }

  private static String[] parsePath(int mode, String[] base, int padding, String path, int off, int size) throws IllegalArgumentException {
    int len = path.length();
    int at = padding + size;
    if (off < len) {
      int pos = path.indexOf('/', off);
      if (pos == -1) {

        //
        String name = path.substring(off);
        if (!NAME_VALIDATOR.matcher(name).matches()) {
          throw new IllegalArgumentException("The name " + name + " is not valid");
        }

        // Find the index of the first '.'
        int cur = name.indexOf('.');
        String[] ret = new String[padding + size + 2];
        System.arraycopy(base, 0, ret, 0, padding);
        if (cur == 0) {
          throw new UnsupportedOperationException("Handle me gracefully");
        } else if (cur == -1) {
          ret[at] = name;
          return ret;
        } else {
          ret[at] = name.substring(0, cur);
          ret[at + 1] = name.substring(cur + 1);
          return ret;
        }
      }
      else {
        int diff = pos - off;
        if (diff == 0) {
          return parsePath(mode, base, padding, path, off + 1, size);
        } else {
          if (diff == 1 && path.charAt(off) == '.') {
            switch (mode) {
              case PARSE_CANONICAL:
                throw new IllegalArgumentException("No '.' allowed here");
              case PARSE_ANY:
                // Skip '.'
                return parsePath(mode, base, padding, path, off + 2, size);
              default:
                throw new AssertionError("Should not be here");
            }
          } else if (diff == 2 && path.charAt(off) == '.' && path.charAt(off + 1) == '.') {
            switch (mode) {
              case PARSE_CANONICAL:
                throw new IllegalArgumentException("No '..' allowed here");
              case PARSE_ANY:
                // Skip '..' ?
                if (size > 0) {
                  return parsePath(mode, base, padding, path, off + 3, size - 1);
                } else if (padding > 0) {
                  return parsePath(mode, base, padding - 1, path, off + 3, size);
                } else {
                  throw new IllegalArgumentException("Invalid path");
                }
              default:
                throw new AssertionError("Should not be here");
            }
          }
          for (int i = off;i < pos;i++) {
            if (path.charAt(i) == '.') {
              throw new IllegalArgumentException("No '.' allowed here");
            }
          }
          String[] ret = parsePath(mode, base, padding, path, pos + 1, size + 1);
          if (ret[at] == null) {
            ret[at] = path.substring(off, pos);
          }
          return ret;
        }
      }
    }
    else {
      String[] ret = new String[padding + size + 2];
      System.arraycopy(base, 0, ret, 0, padding);
      ret[at] = "";
      return ret;
    }
  }

  /**
   * Parse a dot separated name.
   *
   * @param s the sequence
   * @param from the from index
   * @param end the end index
   * @return the parsed identifiers
   * @throws IllegalArgumentException
   */
  static String[] parseName(CharSequence s, int from, int end) throws IllegalArgumentException {
    if (from < 0) {
      throw new IllegalArgumentException("From bound " + from + " cannot be negative");
    }
    if (from > end) {
      throw new IllegalArgumentException("From bound " + from + " cannot be greater than the end bound " + end);
    }
    if (from == end) {
      return Name.EMPTY_STRING_ARRAY;
    } else {
      return parseName(0, s, from, end);
    }
  }

  private static String[] parseName(int size, CharSequence s, int from, int end) {
    int next = Tools.indexOf(s, '.', from, end);
    String[] identifiers;
    if (next < 0) {
      if (from == end) {
        throw new IllegalArgumentException("Empty segment");
      }
      identifiers = new String[size + 1];
      identifiers[size] = s.subSequence(from, end).toString();
    } else {
      if (next == from) {
        throw new IllegalArgumentException("Empty segment");
      }
      identifiers = parseName(size + 1, s, next + 1, end);
      identifiers[size] = s.subSequence(from, next).toString();
    }
    return identifiers;
  }
}
