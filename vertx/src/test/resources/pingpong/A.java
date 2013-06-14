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

package pingpong;

import juzu.Response;
import juzu.View;
import juzu.io.Chunk;
import juzu.io.ChunkBuffer;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;

import javax.inject.Inject;

public class A {

  @Inject
  Vertx vertx;

  @View
  public Response.Content index() throws Exception {
    final ChunkBuffer content = new ChunkBuffer();
    vertx.eventBus().send("foo", "ping", new Handler<Message<String>>() {
      public void handle(Message<String> event) {
        try {
          content.append(Chunk.create(event.body));
        }
        finally {
          content.close();
        }
      }
    });
   return Response.content(200, content);
  }
}