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
   * The parameter pattern as a valid regular expression.
   *
   * @return the pattern value
   */
  String pattern() default "";

}
