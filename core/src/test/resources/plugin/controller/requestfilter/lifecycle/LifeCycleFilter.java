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

package plugin.controller.requestfilter.lifecycle;

import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.impl.request.Stage;
import juzu.test.Registry;
import juzu.Response;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LifeCycleFilter implements RequestFilter<Stage.Handler> {
  public LifeCycleFilter() {
    Registry.compareAndSet("request.filter.lifecycle", null, "created");
  }

  @Override
  public Class<Stage.Handler> getStageType() {
    return Stage.Handler.class;
  }

  @Override
  public Response handle(Stage.Handler argument) {
    Request request = argument.getRequest();
    Registry.compareAndSet("request.filter.lifecycle", "created", "before");
    Response result = argument.invoke();
    Registry.compareAndSet("request.filter.lifecycle", "before", "after");
    return result;
  }
}
