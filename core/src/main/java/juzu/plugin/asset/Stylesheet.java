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

package juzu.plugin.asset;

import juzu.asset.AssetLocation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A stylesheet asset.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Stylesheet {

  /**
   * Returns the stylesheet id, the value is optional and has meaning when this stylesheet must be referenced (for
   * instance as a dependency of another stylesheet).
   *
   * @return the script id
   */
  String id() default "";

  /**
   * Return the stylesheet dependencies, i.e the stylesheet that should be executed before the stylesheet determined by
   * this annotation.
   *
   * @return the dependencies
   */
  String[] depends() default {};

  /**
   * The stylesheet location.
   *
   * @return the location
   */
  AssetLocation location() default AssetLocation.APPLICATION;

  /**
   * The stylesheet source.
   *
   * @return the source
   */
  String src();

}
