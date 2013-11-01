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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * <p>A name as defined by the Java Language Specification. The semantic is slightly different as this
 * name allows a name with zero identifiers. A name can either be:</p>
 *
 * <ul>
 *   <li>An empty name (not in the original JLS) consisting of no identifiers</li>
 *   <li>A simple name consisting of a single identifier</li>
 *   <li>A qualified name consisting of a sequence of identifiers separated by "." tokens</li>
 * </ul>
 *
 * <p>The empty name has been added for making programming easier and avoiding null checks.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Name implements Iterable<String>, Serializable, CharSequence {

  /** . */
  static final String[] EMPTY_STRING_ARRAY = new String[0];

  /** . */
  private static final Name EMPTY = new Name("", EMPTY_STRING_ARRAY);

  /**
   * Create a name from the specified class type argument.
   *
   * @param type the type
   * @return the name
   */
  public static Name create(Class<?> type) throws NullPointerException {
    return parse(type.getName());
  }

  /**
   * Parse the name.
   *
   * @param s the char sequence to parse
   * @return the name
   * @throws IllegalArgumentException if the name is not valid
   */
  public static Name parse(CharSequence s) throws IllegalArgumentException {
    int len = s.length();
    return parse(s, 0, len);
  }

  /**
   * Parse the name.
   *
   * @param s the char sequence to parse
   * @param from the index of the first char of the name
   * @param end the index of the char after the last char of the name
   * @return the name
   * @throws IllegalArgumentException if the name is not valid
   */
  public static Name parse(CharSequence s, int from, int end) throws IllegalArgumentException {
    if (end > s.length()) {
      throw new IllegalArgumentException("End bound " + end + " cannot be greater than the sequence length " + s.length());
    }
    String[] segments = Lexers.parseName(s, from, end);
    if (segments.length == 0) {
      return EMPTY;
    } else {
      return new Name(s.subSequence(from, end).toString(), segments);
    }
  }

  /** . */
  final String[] identifiers;

  /** . */
  final int size;

  /** . */
  private String value;

  /** . */
  private Name parent;

  private Name(String value, String[] identifiers) {
    this(value, identifiers, identifiers.length);
  }

  private Name(String value, String[] identifiers, int size) {
    this.value = value;
    this.identifiers = identifiers;
    this.size = size;
    this.parent = null;
  }

  Name(String[] identifiers) {
    this(identifiers, identifiers.length);
  }

  Name(String[] identifiers, int size) {
    this.value = Tools.join('.', identifiers, 0, size);
    this.identifiers = identifiers;
    this.size = size;
    this.parent = null;
  }

  public Name getParent() {
    if (parent == null) {
      if (size == 1) {
        parent = EMPTY;
      } else if (size() > 1) {
        parent = new Name(value.substring(0, value.length() - 1 - identifiers[size - 1].length()), identifiers, size - 1);
      }
    }
    return parent;
  }

  /**
   * Return true for a empty name
   *
   * @return true for a empty name
   */
  public boolean isEmpty() {
    return size == 0;
  }

  /**
   * Return true for a simple name
   *
   * @return true for a simple name
   */
  public boolean isSimple() {
    return size == 1;
  }

  /**
   * Return true for a qualified name
   *
   * @return true for a qualified name
   */
  public boolean isQualified() {
    return size > 1;
  }

  /**
   * Returns the name identifier or null when the name is empty.
   *
   * @return the identifier
   */
  public String getIdentifier() {
    return size > 0 ? identifiers[size - 1] : null;
  }

  public String toString() {
    return value;
  }

  public int length() {
    return value.length();
  }

  public int size() {
    return size;
  }

  public String get(int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("Index " + index + " cannot be negative");
    }
    if (index >= size) {
      throw new IndexOutOfBoundsException("Index " + index + " cannot be greater than size");
    }
    return identifiers[index];
  }

  public Iterator<String> iterator() {
    return Tools.iterator(0, size, identifiers);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  public char charAt(int index) {
    return value.charAt(index);
  }

  public CharSequence subSequence(int start, int end) {
    return value.subSequence(start, end);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof Name) {
      Name that = (Name)obj;
      if (size == that.size) {
        for (int i = size - 1;i >= 0;i--) {
          if (!identifiers[i].equals(that.identifiers[i])) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  public Path.Absolute resolve(String path) {
    String[] atoms = Lexers.parsePath(Lexers.PARSE_ANY, identifiers, size, path, 0);
    return new Path.Absolute(new Name(atoms, atoms.length - 1), atoms[atoms.length - 1]);
  }

  /**
   * Resolve a path with respect to this name and return an absolute path.
   *
   * @param path the path
   * @return the corresponding absolute path
   */
  public Path.Absolute resolve(Path path) {
    if (path instanceof Path.Absolute) {
      return (Path.Absolute)path;
    } else {
      return Path.absolute(append(path.getName()), path.getExt());
    }
  }

  /**
   * Create and return a new name using the suffix argument appended to this name.
   *
   * @param suffix the suffix
   * @return the new name
   * @throws NullPointerException if the suffix is null
   */
  public Name append(Name suffix) throws NullPointerException {
    if (suffix == null) {
      throw new NullPointerException("No null suffix allowed");
    }
    return append(suffix.identifiers, suffix.size);
  }

  /**
   * Create and return a new name using the suffix argument appended to this name.
   *
   * @param identifiers the identifiers suffix
   * @return the new name
   * @throws NullPointerException if the suffix is null
   * @throws IllegalArgumentException if the suffix is not valid
   */
  public Name append(String... identifiers) throws NullPointerException, IllegalArgumentException {
    if (identifiers == null) {
      throw new NullPointerException("No null suffix allowed");
    }
    return append(identifiers, identifiers.length);
  }

  public Name subName(int l) throws IllegalArgumentException {
    if (l < 0) {
      throw new IllegalArgumentException("No negative argument " + l + " accepted");
    } else if (l == 0) {
      return this;
    } else {
      int remaining = size - l;
      if (remaining < 0) {
        throw new IllegalArgumentException("Argument " + l + " can't be greater than size " + size);
      } else if (remaining == 0) {
        return EMPTY;
      } else {
        String[] identifiers = new String[remaining];
        System.arraycopy(this.identifiers, l, identifiers, 0, remaining);
        return new Name(identifiers);
      }
    }
  }

  private Name append(String[] suffixIdentifiers, int suffixSize) throws NullPointerException, IllegalArgumentException {
    if (size == 0) {
      return this;
    }
    else {
      String[] tmp = new String[size + suffixSize];
      System.arraycopy(identifiers, 0, tmp, 0, this.size);
      System.arraycopy(suffixIdentifiers, 0, tmp, this.size, suffixSize);
      int len = value.length();
      for (int i = 0;i < suffixSize;i++) {
        if (suffixIdentifiers[i] == null) {
          throw new IllegalArgumentException("Cannot accept null suffix segment");
        }
        if (suffixIdentifiers[i].isEmpty()) {
          throw new IllegalArgumentException("Cannot accept empty suffix segment");
        }
        if (suffixIdentifiers[i].indexOf('.') != -1) {
          throw new IllegalArgumentException("Cannot accept '.' in suffix segment");
        }
        len += 1 + suffixIdentifiers[i].length();
      }
      StringBuilder sb = new StringBuilder(len).append(value);
      for (int i = 0;i < suffixSize;i++) {
        sb.append('.').append(suffixIdentifiers[i]);
      }
      return new Name(sb.toString(), tmp, tmp.length);
    }
  }

  /**
   * Returns the common prefix between this qualified name and the provided qualified name.
   *
   * @param name the qualified name
   * @return the common prefix
   */
  public Name getPrefix(Name name) {
    int size = Math.min(this.size, name.size);
    int a = 0;
    int len = 0;
    for (int i = 0;i < size;i++) {
      if (!identifiers[i].equals(name.identifiers[i])) {
        break;
      } else {
        a++;
        if (i > 0) {
          len++;
        }
        len += identifiers[i].length();
      }
    }
    return a == this.size ? this : new Name(value.substring(0, len), identifiers, a);
  }

  public boolean isPrefix(Name name) {
    if (name.parent == this) {
      // The fast way
      return true;
    }
    else if (size <= name.size) {
      for (int i = 0;i < size;i++) {
        if (!identifiers[i].equals(name.identifiers[i])) {
          return false;
        }
      }
      return true;
    }
    else {
      return false;
    }
  }
}
