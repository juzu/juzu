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
