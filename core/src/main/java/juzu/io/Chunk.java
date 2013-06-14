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

  public static Data.CharSequence create(CharSequence data) {
    return new Data.CharSequence(data);
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
    private final T value;

    /** . */
    private final PropertyType<T> type;

    public Property(T value, PropertyType<T> type) {
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
    public static class CharSequence extends Data {

      /** . */
      public final java.lang.CharSequence data;

      private CharSequence(java.lang.CharSequence data) {
        this.data = data;
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
