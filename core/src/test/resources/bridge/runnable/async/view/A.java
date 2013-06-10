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
package bridge.runnable.async.view;

import juzu.Response;
import juzu.View;
import juzu.impl.bridge.runnable.AbstractRunnableAsyncTestCase;
import juzu.impl.inject.ScopeController;
import juzu.impl.request.ContextLifeCycle;
import juzu.impl.request.Request;
import juzu.io.AsyncStreamable;
import juzu.request.RequestContext;
import juzu.request.RequestLifeCycle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A implements RequestLifeCycle {

  /** . */
  private RequestContext context = null;

  public void beginRequest(RequestContext context) {
    this.context = context;
  }

  public void endRequest(RequestContext context) {
    this.context = null;
  }

  @PostConstruct
  public void construct() {
    AbstractRunnableAsyncTestCase.destroyed.set(false);
  }

  @PreDestroy
  public void destroy() {
    AbstractRunnableAsyncTestCase.destroyed.set(true);
  }

  @View
  public Response.Content index() throws IOException {
    final Request request = Request.getCurrent();
    final ScopeController controller = request.getScopeController();
    AbstractRunnableAsyncTestCase.requestURL = "" + A_.index();
    AbstractRunnableAsyncTestCase.requestDestroyed = AbstractRunnableAsyncTestCase.destroyed.get();
    final AsyncStreamable content = new AsyncStreamable();
    Runnable task = new Runnable() {
      public void run() {
        ContextLifeCycle lf = request.suspend();
        try {
          Thread.sleep(500);
          AbstractRunnableAsyncTestCase.runnableURL = "" + A_.index();
          AbstractRunnableAsyncTestCase.runnableDestroyed = AbstractRunnableAsyncTestCase.destroyed.get();
          AbstractRunnableAsyncTestCase.runnableActive = controller.isActive();
          content.append("pass");
        }
        catch (Exception e) {
          e.printStackTrace();
        } finally {
          lf.resume();
          content.close();
        }
      }
    };
    context.getExecutor().execute(task);
    return Response.content(200, content);
  }
}
