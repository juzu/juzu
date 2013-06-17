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

/** @author Julien Viet */
public class StreamableDecorator implements Streamable {

  /** . */
  private final Streamable producer;

  public StreamableDecorator(Streamable producer) {
    this.producer = producer;
  }

  protected void sendHeader(Stream consumer) {

  }

  protected void sendFooter(Stream consumer) {

  }

  public void send(final Stream stream) throws IllegalStateException {
    sendHeader(stream);
    Stream wrapper = new Stream() {
      public void provide(Chunk chunk) {
        stream.provide(chunk);
      }
      public void close(Thread.UncaughtExceptionHandler errorHandler) {
        try {
          sendFooter(stream);
        }
        finally {
          stream.close(errorHandler);
        }
      }
    };
    producer.send(wrapper);
  }
}
