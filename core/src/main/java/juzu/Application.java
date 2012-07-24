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
   * Controls if the generated URL should be escaped to valid XML.
   *
   * @return the escape XML value
   */
  boolean escapeXML() default false;

}
