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

/**
 * This interface can be implemented by a controller to be aware of the dispatch of a request on the controller.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
// tag::class[]
public interface RequestLifeCycle {

  /**
   * <p>Signals to the controller that a request begins. During the invocation of this method,
   * if a {@link juzu.Response} is set on the request context, the request will be considered
   * as terminated.</p>
   *
   * <p>When this method throws a runtime exception, a {@link juzu.Response.Error} response will
   * be set on the request context, thus terminating the request.</p>
   *
   * @param context the request context
   */
  void beginRequest(RequestContext context);

  /**
   * <p>Signals to the controller that a request ends. During the invocation of this method,
   * the response set during the dispatch of the request is available via the
   * {@link juzu.request.RequestContext#getResponse()} method, this method is free to override
   * it and provide a new response instead.</p>
   *
   * <p>When this method throws a runtime exception, a {@link juzu.Response.Error} response
   * will be set on the request context, thus terminating the request.</p>
   *
   * @param context the request context
   */
  void endRequest(RequestContext context);

}
// end::class[]
