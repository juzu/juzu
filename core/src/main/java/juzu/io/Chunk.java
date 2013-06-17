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
package juzu.io;

import juzu.PropertyType;

/**
 * A chunk of data.
 *
 * @author Julien Viet
 */
public abstract class Chunk {

  public static Data.Char create(char c) {
    return new Data.Char(c);
  }

  public static Data.CharSequence create(CharSequence data) {
    return create(data, 0, data.length());
  }

  public static Data.CharSequence create(CharSequence data, int start, int end) {
    return new Data.CharSequence(data, start, end);
  }

  public static Data.InputStream create(java.io.InputStream data) {
    return new Data.InputStream(data);
  }

  public static Data.Readable create(Readable data) {
    return new Data.Readable(data);
  }

  public static Data.Chars create(char[] data) {
    return new Data.Chars(data);
  }

  public static Data.Bytes create(byte[] data) {
    return new Data.Bytes(data);
  }

  /**
   * A property chunk.
   * @param <T>
   */
  public static class Property<T> extends Chunk {

    /** . */
    public final T value;

    /** . */
    public final PropertyType<T> type;

    public Property(T value, PropertyType<T> type) {
      if (type == null) {
        throw new NullPointerException("Property type cannot be null for value " + value);
      }
      if (value == null) {
        throw new NullPointerException("Property value cannot be null for type " + type);
      }
      this.value = value;
      this.type = type;
    }
  }

  /**
   * A data chunk.
   */
  public static class Data extends Chunk {

    /**
     * A chars chunk.
     */
    public static class Readable extends Data {

      /** . */
      public final java.lang.Readable data;

      private Readable(java.lang.Readable data) {
        this.data = data;
      }
    }

    /**
     * A chars chunk.
     */
    public static class InputStream extends Data {

      /** . */
      public final java.io.InputStream data;

      private InputStream(java.io.InputStream data) {
        this.data = data;
      }
    }

    /**
     * A chars chunk.
     */
    public static class Char extends Data {

      /** . */
      public final char value;

      public Char(char value) {
        this.value = value;
      }
    }

    /**
     * A chars chunk.
     */
    public static class CharSequence extends Data {

      /** . */
      public final java.lang.CharSequence data;

      /** . */
      public final int start;

      /** . */
      public final int end;

      private CharSequence(java.lang.CharSequence data, int start, int end) {
        this.data = data;
        this.start = start;
        this.end = end;
      }
    }

    /**
     * A chars chunk.
     */
    public static class Chars extends Data {

      /** . */
      public final char[] data;

      private Chars(char[] data) {
        this.data = data;
      }
    }

    /**
     * A byte chunk.
     */
    public static class Bytes extends Data {

      /** . */
      public final byte[] data;

      private Bytes(byte[] data) {
        this.data = data;
      }
    }
  }
}
