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
package juzu.impl.request;

import juzu.Scope;
import juzu.impl.bridge.spi.ScopedContext;
import juzu.impl.common.Tools;
import juzu.request.Phase;

/** @author Julien Viet */
public class ContextLifeCycle {

  private Request request;

  public ContextLifeCycle(Request request) {
    this.request = request;
  }

  /**
   * Resume the current context life cycle.
   */
  public void resume() {

    //
    ContextLifeCycle lifeCycle = Request.current.get();
    if (lifeCycle != null) {
      throw new IllegalStateException("A context is already active for this thread");
    }

    //
    Request.current.set(this);
  }

  Request getRequest() {
    return request;
  }

  /**
   * End the current contextual, this method should not throw anything
   */
  void endContextual() {

    // Remove
    request.contextLifeCycles.remove(this);

    // Deassociate
    request.getScopeController().end();

    // We are done -> cleanup
    if (request.contextLifeCycles.isEmpty()) {

      // Dispose controller first
      if (request.controllerLifeCycle != null) {
        request.controllerLifeCycle.close();
      }

      // End scopes
      if (request.getPhase() == Phase.VIEW) {
        ScopedContext flashScope = request.bridge.getScopedContext(Scope.FLASH, false);
        if (flashScope != null) {
          Tools.safeClose(flashScope);
        }
      }
      ScopedContext requestScope = request.bridge.getScopedContext(Scope.REQUEST, false);
      if (requestScope != null) {
        Tools.safeClose(requestScope);
      }
    }
  }
}
