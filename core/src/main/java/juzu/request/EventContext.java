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

package juzu.request;

import juzu.impl.bridge.spi.EventBridge;
import juzu.impl.request.Method;
import juzu.impl.request.Request;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class EventContext extends RequestContext {

  /** . */
  private final EventBridge bridge;

  public EventContext(Request request, Method method, EventBridge bridge) {
    super(request, method);

    //
    this.bridge = bridge;
  }

  @Override
  public Phase.Event getPhase() {
    return Phase.EVENT;
  }

  @Override
  protected EventBridge getBridge() {
    return bridge;
  }
}
