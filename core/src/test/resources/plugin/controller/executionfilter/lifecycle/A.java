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

package plugin.controller.executionfilter.lifecycle;

import juzu.Response;
import juzu.View;
import juzu.impl.plugin.controller.ExecutionFilterTestCase;
import juzu.request.RequestContext;
import juzu.request.RequestLifeCycle;

import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A implements RequestLifeCycle {

  /** . */
  RequestContext context;

  public void beginRequest(RequestContext context) {
    this.context = context;
  }

  public void endRequest(RequestContext context) {
    this.context = null;
  }

  @View
  public Response.Content index() throws Exception {
    FutureTask<String> task = new FutureTask<String>(new Runnable() {
      public void run() {
        ExecutionFilterTestCase.events.add("run");
      }
    }, "hello");
    Executor executor = context.getExecutor();
    ExecutionFilterTestCase.events.add("execute");
    executor.execute(task);
    String s = task.get();
    ExecutionFilterTestCase.events.add(s);
    return Response.ok("foo");
  }
}
