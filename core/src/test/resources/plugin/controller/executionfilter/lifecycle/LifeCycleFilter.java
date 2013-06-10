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

import juzu.impl.plugin.controller.ExecutionFilterTestCase;
import juzu.impl.request.ExecutionFilter;
import juzu.impl.request.Request;
import juzu.test.Registry;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LifeCycleFilter implements ExecutionFilter {

  public LifeCycleFilter() {
    Registry.compareAndSet("request.filter.lifecycle", null, "created");
  }

  public void invoke(Request request) {
    Registry.compareAndSet("request.filter.lifecycle", "created", "before");
    request.invoke();
    Registry.compareAndSet("request.filter.lifecycle", "before", "after");
  }

  public Runnable onCommand(final Runnable command) {
    ExecutionFilterTestCase.events.add("onCommand");
    return new Runnable() {
      public void run() {
        ExecutionFilterTestCase.events.add("beforeRun");
        command.run();
        ExecutionFilterTestCase.events.add("afterRun");
      }
    };
  }
}
