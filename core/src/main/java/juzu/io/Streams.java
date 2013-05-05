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

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;


/**
 * Implementation of the {@link juzu.io.Stream.Char} interface that uses an appendable delegate.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Streams {

  private static class Appendable extends BaseCharStream {

    /** . */
    protected final java.lang.Appendable delegate;

    private Appendable(java.lang.Appendable delegate) {
      this.delegate = delegate;
    }

    public Char append(char c) throws IOException {
      delegate.append(c);
      return this;
    }

    public Char append(CharSequence s) throws IOException {
      delegate.append(s);
      return this;
    }

    public Char append(CharSequence csq, int start, int end) throws IOException {
      delegate.append(csq, start, end);
      return this;
    }
  }

  public static Stream.Char empty() {
    return BaseCharStream.INSTANCE;
  }

  public static Stream.Char appendable(java.lang.Appendable appendable) {
    return new Appendable(appendable);
  }

  public static <A extends java.lang.Appendable & Flushable> Stream.Char flushable(final A appendable) {
    return new Appendable(appendable) {
      @Override
      public void flush() throws IOException {
        appendable.flush();
      }
    };
  }

  public static <A extends java.lang.Appendable & Flushable & Closeable> Stream.Char closeable(final A appendable) {
    return new Appendable(appendable) {
      @Override
      public void flush() throws IOException {
        appendable.flush();
      }
      @Override
      public void close() throws IOException {
        appendable.close();
      }
    };
  }
}
