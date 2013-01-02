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

package juzu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a route when Juzu is exposes the application over the HTTP protocol. The route can be used
 * at the application level and at the controller level.
 *
 * <h2>Application routes</h2>
 *
 * <p>An application package can be annotated to <i>mount</i> the application, this is useful when a
 * compilation unit contains several applications and each needs to be accessed with a different route</p>
 *
 * <code><pre>
 *    &#064;{@link Route}("/myapplication")
 *    &#064;{@link Application}
 *    package myapplication;
 * </pre></code>
 *
 * <h2>Controller method routes</h2>
 *
 * <p>Controller methods can be annotated to <i>mount</i> the controller:</p>
 *
 * <code><pre>
 *    public class MyController {
 *
 *       &#064;{@link Action}
 *       &#064;{@link Route}("/myaction/{value}")
 *       public {@link juzu.Response.Render} myAction(String value) { ... }
 *
 *       &#064;{@link View}
 *       &#064;{@link Route}("/myview")
 *       public {@link juzu.Response.Render} myView() { ... }
 *
 *       &#064;{@link Resource}
 *       &#064;{@link Route}("/myresource")
 *       public {@link juzu.Response.Content} myView() { ... }
 *    }
 * </pre></code>
 *
 * The {@link Param} annotation can be used further more for constraining a parameter in the route path.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Route {

  /**
   * The route path.
   *
   * @return the route path
   */
  String value();

  /**
   * The route priority.
   *
   * @return the route priority
   */
  int priority() default 0;

}
