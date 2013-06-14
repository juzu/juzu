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

package async;

import juzu.Response;
import juzu.View;
import juzu.io.Chunk;
import juzu.io.ChunkBuffer;

public class A {

  @View
  public Response.Content index() throws Exception {
    final ChunkBuffer content = new ChunkBuffer();
    new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(500);
          content.append(Chunk.create("pass"));
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        finally {
          content.close();
        }
      }
    }.start();
    return Response.content(200, content);
  }
}