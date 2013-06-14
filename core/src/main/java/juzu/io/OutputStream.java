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

import juzu.impl.common.Tools;
import juzu.impl.io.AppendableStream;
import juzu.impl.io.BinaryOutputStream;
import juzu.impl.io.SinkStream;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;

/** @author Julien Viet */
public abstract class OutputStream implements Stream {

  /** . */
  public static final int BUFFER_SIZE = 512;

  /** . */
  private LinkedList<Error> errors = null;

  public static OutputStream create(Charset charset, Appendable out) {
    return new AppendableStream(charset, out);
  }

  public static OutputStream create(Charset charset, Appendable out, Flushable flushable, Closeable closeable) {
    return new AppendableStream(charset, out, flushable, closeable);
  }

  public static OutputStream create(Charset charset, java.io.OutputStream out) {
    return new BinaryOutputStream(charset, out);
  }

  public static SinkStream create() {
    return SinkStream.INSTANCE;
  }

  public final void provide(Chunk chunk) {
    try {
      if (chunk instanceof Chunk.Data) {
        Chunk.Data data = (Chunk.Data)chunk;
        if (data instanceof Chunk.Data.Bytes) {
          append(((Chunk.Data.Bytes)data).data);
        } else if (data instanceof Chunk.Data.Chars) {
          append(CharBuffer.wrap(((Chunk.Data.Chars)data).data));
        } else if (data instanceof Chunk.Data.CharSequence) {
          append(((Chunk.Data.CharSequence)data).data);
        } else if (data instanceof Chunk.Data.InputStream) {
          ByteArrayOutputStream baos = Tools.copy(((Chunk.Data.InputStream)data).data, new ByteArrayOutputStream());
          append(baos.toByteArray());
        } else if (data instanceof Chunk.Data.Readable) {
          Readable readable = ((Chunk.Data.Readable)data).data;
          CharBuffer buffer = CharBuffer.allocate(512);
          for (int i = readable.read(buffer);i != -1;i = readable.read(buffer)) {
            buffer.flip();
            append(buffer);
            buffer.clear();
          }
          if (readable instanceof Closeable) {
            Tools.safeClose((Closeable)readable);
          }
        } else {
          throw new IOException("Not yet handled");
        }
      }
    }
    catch (IOException e) {
      reportError(e);
    }
  }

  public void close(Thread.UncaughtExceptionHandler errorHandler) {
    try {
      close();
    }
    catch (Exception e) {
      reportError(e);
    }
    if (errorHandler != null && errors != null) {
      for (Error error : errors) {
        errorHandler.uncaughtException(error.thread, error.uncaught);
      }
    }
  }

  private void reportError(Throwable t) {
    if (errors == null) {
      errors = new LinkedList<Error>();
    }
    errors.add(new Error(Thread.currentThread(), t));
  }

  public abstract void append(CharBuffer buffer) throws IOException;

  public abstract void append(CharSequence csq) throws IOException;

  public abstract void append(CharSequence csq, int start, int end) throws IOException;

  public abstract void append(ByteBuffer buffer) throws IOException;

  public abstract void append(char c) throws IOException;

  public abstract void append(byte[] data) throws IOException;

  public abstract void append(byte[] data, int off, int len) throws IOException;

  private static class Error {
    final Thread thread;
    final Throwable uncaught;
    private Error(Thread thread, Throwable uncaught) {
      this.thread = thread;
      this.uncaught = uncaught;
    }
  }
}
