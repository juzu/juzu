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
package bridge.runnable.contextualasync.resource;

import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.impl.bridge.runnable.AbstractRunnableContextualAsyncTestCase;
import juzu.impl.request.Request;
import juzu.io.Chunk;
import juzu.io.ChunkBuffer;
import juzu.request.RequestContext;
import juzu.request.RequestLifeCycle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A implements RequestLifeCycle {

  /** . */
  private RequestContext context;

  public void beginRequest(RequestContext context) {
    this.context = context;
  }

  public void endRequest(RequestContext context) {
    this.context = null;
  }

  @PostConstruct
  public void construct() {
    AbstractRunnableContextualAsyncTestCase.destroyed.set(false);
  }

  @PreDestroy
  public void destroy() {
    AbstractRunnableContextualAsyncTestCase.destroyed.set(true);
  }

  @Resource
  @Route("/")
  public Response.Content index() throws IOException {
    AbstractRunnableContextualAsyncTestCase.requestURL = "" + A_.index();
    AbstractRunnableContextualAsyncTestCase.requestDestroyed = AbstractRunnableContextualAsyncTestCase.destroyed.get();
    final ChunkBuffer content = new ChunkBuffer();
    Runnable task = new Runnable() {
      public void run() {
        try {
          Thread.sleep(500);
          AbstractRunnableContextualAsyncTestCase.runnableURL = "" + A_.index();
          AbstractRunnableContextualAsyncTestCase.runnableDestroyed = AbstractRunnableContextualAsyncTestCase.destroyed.get();
          AbstractRunnableContextualAsyncTestCase.runnableActive = Request.getCurrent().getScopeController().isActive();
          content.append(Chunk.create("pass"));
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    context.getExecutor().execute(task);
    return Response.content(200, content);
  }
}
