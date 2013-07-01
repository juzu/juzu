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
package juzu.impl.bridge.spi.web;

import juzu.PropertyType;
import juzu.io.Chunk;
import juzu.io.Stream;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Map;

/** @author Julien Viet */
public abstract class HttpStream implements AsyncStream {

  /** . */
  private static final int STATUS_BUFFERING = 0;

  /** . */
  private static final int STATUS_STREAMING = 1;

  /** . */
  private static final int STATUS_CLOSED = 2;

  /** . */
  protected Charset encoding;

  /** . */
  protected String mimeType;

  /** . */
  protected LinkedList<Map.Entry<String, String[]>> headers;

  /** . */
  private int status;

  /** . */
  private final WebRequestContext context;

  public HttpStream(WebRequestContext context, int statusCode, Charset encoding) {

    //
    this.context = context;
    this.status = STATUS_BUFFERING;
    this.headers = new LinkedList<Map.Entry<String, String[]>>();
    this.mimeType = null;
    this.encoding = encoding;

    //
    setStatusCode(statusCode);
  }

  public abstract void setStatusCode(int status);

  public void provide(Chunk chunk) {
    if (status == STATUS_BUFFERING) {
      if (chunk instanceof Chunk.Property<?>) {
        Chunk.Property<?> property = (Chunk.Property<?>)chunk;
        if (property.type == PropertyType.ENCODING) {
          encoding = (Charset)property.value;
        } else if (property.type == PropertyType.MIME_TYPE) {
          mimeType = (String)property.value;
        } else if (property.type == PropertyType.HEADER) {
          headers.addLast((Map.Entry<String, String[]>)property.value);
        }
      } else if (chunk instanceof Chunk.Data) {
        sendHeaders();
        status = STATUS_STREAMING;
      }
    }
    if (status == STATUS_STREAMING) {
      if (chunk instanceof Chunk.Data) {
        getDataStream(true).provide(chunk);
      }
    }
  }

  protected abstract Stream getDataStream(boolean create);

  private void sendHeaders() {
    if (mimeType != null) {
      context.setContentType(mimeType, encoding);
    }
    context.setHeaders(headers);
  }

  public void close(Thread.UncaughtExceptionHandler errorHandler) {
    if (status == STATUS_BUFFERING) {
      sendHeaders();
    }
    if (status != STATUS_CLOSED) {
      status = STATUS_CLOSED;
      Stream dataStream = getDataStream(false);
      if (dataStream != null) {
        dataStream.close(errorHandler);
      }
      endAsync();
    }
  }

  public void end() {
    if (status != STATUS_CLOSED) {
      beginAsync();
    }
  }

  protected abstract void endAsync();

  protected  abstract void beginAsync();

}
