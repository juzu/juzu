/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.request;

/**
 * This interface can be implemented by a controller to be aware of the dispatch of a request on the controller.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface RequestLifeCycle {

  /**
   * <p>Signals to the controller that a request begins. During the invocation of this method, if a {@link juzu.Response}
   * is set on the request context, the request will be considered as terminated.</p>
   *
   * <p>When this method throws a runtime exception, a {@link juzu.Response.Error} response will be set on the request
   * context, thus terminating the request.</p>
   *
   * @param context the request context
   */
  void beginRequest(RequestContext context);

  /**
   * <p>Signals to the controller that a request ends. During the invocation of this method, the response set during
   * the dispatch of the request is available via the {@link juzu.request.RequestContext#getResponse()} method, this
   * method is free to override it and provide a new response instead.</p>
   *
   * <p>When this method throws a runtime exception, a {@link juzu.Response.Error} response will be set on the request
   * context, thus terminating the request.</p>
   *
   * @param context the request context
   */
  void endRequest(RequestContext context);

}
