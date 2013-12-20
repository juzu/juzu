/*
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
package juzu.plugin.amd;

import juzu.plugin.asset.Asset;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a JavaScript managed module.
 *
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Module {

  /**
   * The module asset.
   *
   * @return the asset
   */
  Asset value();

  /**
   * An optional adapter.
   *
   * @return the module adapter
   */
  String adapter() default "";

  /**
   * A set of dependencies
   *
   * @return the dependencies
   */
  Dependency[] dependencies() default {};

}
