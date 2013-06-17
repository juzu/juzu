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

package juzu.impl.io;

import juzu.io.Chunk;
import juzu.io.Stream;

import java.util.LinkedList;

/**
 * An stream that handles error logging.
 *
 * @author Julien Viet
 */
public final class SafeStream implements Stream {

  /** . */
  private LinkedList<Exception> errors;

  /** . */
  private final Stream delegate;

  public SafeStream(Stream delegate) {
    this.delegate = delegate;
  }

  private void log(Exception error) {
    if (errors == null) {
      errors = new LinkedList<Exception>();
    }
    errors.add(error);
  }

  public void provide(Chunk chunk) {
    try {
      delegate.provide(chunk);
    }
    catch (Exception e) {
      log(e);
    }
  }

  public void close(Thread.UncaughtExceptionHandler errorHandler) {
    try {
      delegate.close(errorHandler);
    }
    catch (Exception e) {
      log(e);
    }
    if (errorHandler != null && errors != null) {
      for (Exception error : errors) {
        errorHandler.uncaughtException(null, error);
      }
    }
  }
}
