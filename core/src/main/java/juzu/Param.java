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
 * An annotation describing a parameter.
 *
 * <h2>Annotating a route parameter</h2>
 *
 * <p>Route parameters can be annotated to provide constrain the value of a parameter, for instance:</p>
 *
 * <code><pre>
 *    public class MyController {
 *
 *       &#064;{@link View}
 *       &#064;{@link Route}("/myview/{id}")
 *       public {@link juzu.Response.Render} myView(&#064;{@link Param}(pattern = "[0-9]+") String id) { ... }
 *    }
 * </pre></code>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Param {


  /**
   * The parameter name, when set overrides the introspected name. This is useful when a parameter name cannot
   * be expressed as a valid java identifier, for example a parameter name containing a dot character.
   *
   * @return the parametre name
   */
  String name() default "";

  /**
   * The parameter pattern as a valid regular expression.
   *
   * @return the pattern value
   */
  String pattern() default "";

  boolean preservePath() default false;

  boolean captureGroup() default false;

}
