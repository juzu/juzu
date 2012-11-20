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

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines the path to a resource that can be injected in Juzu.
 *
 * <h2>Injecting templates</h2>
 *
 * <p>Templates can be injected in a Juzu applications, the templates will be located thanks to the path
 * {@link #value()} of the annotation and injected as a {@link juzu.template.Template} class. For example, a
 * template can be injected in a controller:</p>
 *
 * <code><pre>
 *    public class MyController {
 *
 *       &#064;Inject &#064;{@link Path}("index.gtmpl") {@link juzu.template.Template} index;
 *
 *       &#064;{@link View}
 *       public {@link juzu.Response.Render} myView() {
 *          return index.render();
 *       }
 *    }
 * </pre></code>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface Path {

  /**
   * The path value.
   *
   * @return the path value
   */
  String value();

}
