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
import java.util.concurrent.Future;

/**
 * A stream is a consumer of chunk.
 *
 * @author Julien Viet
 */
public interface Stream extends Closeable {

  /**
   * Provide a chunk.
   *
   * @param chunk the chunk
   */
  void provide(Chunk chunk);

  /**
   * Signal the work is done.
   */
  void close();

  /**
   * Signal the work is done, the caller can be aware of error that were not caught. The <code>errorHandler</code>
   * argument can be null.
   *
   * @param errorHandler the optional error handler
   */
  void close(Thread.UncaughtExceptionHandler errorHandler);

}
