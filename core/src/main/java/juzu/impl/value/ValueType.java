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
package juzu.impl.value;

import juzu.Format;
import juzu.impl.common.Tools;

import java.lang.reflect.AnnotatedElement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * The value type performs a bidirectional conversion between a value object and a string.
 *
 * @author Julien Viet
 */
public abstract class ValueType<T> {

  /**
   * The list of java classes this implementation can handle.
   *
   * @return the list of types.
   */
  public abstract Iterable<Class<?>> getTypes();

  /**
   * Parse a string and returns the corresponding value type.
   *
   * @param element the element annotations
   * @param s the string to parse
   * @return the corresponding value
   * @throws java.lang.Exception any exception preventing the parse to succeed
   */
  public abstract T parse(AnnotatedElement element, String s) throws Exception;

  /**
   * Format a value and returns the corresponding string.
   *
   * @param element the element annotations
   * @param value the value to format
   * @return the corresponding string
   */
  public abstract String format(AnnotatedElement element, T value);

  public static ValueType<String> STRING = new ValueType<String>() {

    /** . */
    private final Iterable<Class<?>> TYPES = Collections.<Class<?>>singleton(String.class);

    @Override
    public Iterable<Class<?>> getTypes() {
      return TYPES;
    }

    @Override
    public String parse(AnnotatedElement element, String s) {
      return s;
    }

    @Override
    public String format(AnnotatedElement element, String value) {
      return value;
    }
  };

  public static ValueType<Integer> INTEGER = new ValueType<Integer>() {

    /** . */
    private final Iterable<Class<?>> TYPES = Tools.<Class<?>>safeUnmodifiableList(Integer.class, int.class);

    @Override
    public Iterable<Class<?>> getTypes() {
      return TYPES;
    }

    @Override
    public Integer parse(AnnotatedElement element, String s) {
      return Integer.parseInt(s);
    }

    @Override
    public String format(AnnotatedElement element, Integer value) {
      return value.toString();
    }
  };

  public static ValueType<Boolean> BOOLEAN = new ValueType<Boolean>() {

    /** . */
    private final Iterable<Class<?>> TYPES = Tools.<Class<?>>safeUnmodifiableList(Boolean.class, boolean.class);

    @Override
    public Iterable<Class<?>> getTypes() {
      return TYPES;
    }

    @Override
    public Boolean parse(AnnotatedElement element, String s) {
      return Boolean.parseBoolean(s);
    }

    @Override
    public String format(AnnotatedElement element, Boolean value) {
      return value.toString();
    }
  };

  public static ValueType<Date> DATE = new ValueType<Date>() {

    /** . */
    private final Iterable<Class<?>> TYPES = Tools.<Class<?>>safeUnmodifiableList(Date.class);

    @Override
    public Iterable<Class<?>> getTypes() {
      return TYPES;
    }

    private SimpleDateFormat getSimpleDateFormat(AnnotatedElement element) {
      Format format = element.getAnnotation(Format.class);
      return format != null ? new SimpleDateFormat(format.value()) : new SimpleDateFormat();
    }

    @Override
    public Date parse(AnnotatedElement element, String s) {
      try {
        return getSimpleDateFormat(element).parse(s);
      }
      catch (ParseException e) {
        throw new UnsupportedOperationException("Handle me gracefully", e);
      }
    }

    @Override
    public String format(AnnotatedElement element, Date value) {
      return getSimpleDateFormat(element).format(value);
    }
  };

  /**
   * Builtins value types.
   */
  public static final List<ValueType<?>> DEFAULT = Tools.<ValueType<?>>safeUnmodifiableList(
      STRING, INTEGER, BOOLEAN, DATE
  );
}
