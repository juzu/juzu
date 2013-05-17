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

package juzu.bridge.vertx;

import juzu.io.BinaryStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerResponse;

import java.io.IOException;
import java.nio.charset.Charset;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class VertxStream extends BinaryStream {

  /** . */
  private final HttpServerResponse resp;

  public VertxStream(Charset charset, HttpServerResponse resp) {
    super(charset);

    //
    resp.setChunked(true);

    //
    this.resp = resp;
  }

  @Override
  public BinaryStream append(byte[] data, int off, int len) throws IOException {
    resp.write(new Buffer(ChannelBuffers.wrappedBuffer(data, off, len)));
    return this;
  }

  @Override
  public BinaryStream append(byte[] data) throws IOException {
    resp.write(new Buffer(ChannelBuffers.wrappedBuffer(data)));
    return this;
  }

  public void close() throws IOException {
    resp.end();
    resp.close();
  }

  public void flush() throws IOException {
  }
}
