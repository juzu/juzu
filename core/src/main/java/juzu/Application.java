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
 * <p>A package annotation for declaring an application.</p> <p/> <p>By default the application name is the current
 * package name whose first letter is capitalized and appended with the word <i>Applicaiton</i>. For instance when this
 * annotation annotates the package <code>org.hello</code> the resulting application name will be
 * <code>HelloApplication</code>. The application name can be overrided thanks to the {@link #name} annotation
 * parameter.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface Application {

  /**
   * Returns an optional application name.
   *
   * @return the application name
   */
  String name() default "";

  /**
   * Returns an optional default controller class.
   *
   * @return the default controller
   */
  Class<?> defaultController() default Object.class;

  /**
   * Controls if the application generated URL should be escaped to valid XML, this is valid for portlet applications.
   *
   * @return the escape XML value
   */
  boolean escapeXML() default false;

}
