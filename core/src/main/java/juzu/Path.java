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
