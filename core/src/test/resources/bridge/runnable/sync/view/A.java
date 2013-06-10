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
package bridge.runnable.sync.view;

import juzu.Response;
import juzu.View;
import juzu.impl.bridge.runnable.AbstractRunnableSyncTestCase;
import juzu.impl.inject.ScopeController;
import juzu.impl.request.ContextLifeCycle;
import juzu.impl.request.Request;
import juzu.request.RequestContext;
import juzu.request.RequestLifeCycle;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A implements RequestLifeCycle {

  /** . */
  private RequestContext context = null;

  /** . */
  private final ThreadLocal<Object> our = new ThreadLocal<Object>();

  public void beginRequest(RequestContext context) {
    this.context = context;
  }

  public void endRequest(RequestContext context) {
    this.context = null;
  }

  @View
  public Response.Content index() throws IOException {
    Request request = Request.getCurrent();
    final ScopeController controller = request.getScopeController();
    Object obj = new Object();
    AbstractRunnableSyncTestCase.requestURL = "" + A_.index();
    AbstractRunnableSyncTestCase.requestObject = obj;
    our.set(obj);
    Runnable task = new Runnable() {
      public void run() {
        AbstractRunnableSyncTestCase.runnableURL = "" + A_.index();
        AbstractRunnableSyncTestCase.runnableObject = our.get();
        AbstractRunnableSyncTestCase.runnableActive = controller.isActive();
      }
    };
    ContextLifeCycle clf = request.suspend();
    try {
      task.run();
    }
    finally {
      clf.resume();
    }
    return Response.ok("pass");
  }
}
