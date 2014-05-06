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
 * A script asset declaration.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Script {

  /**
   * @return the asset id, used for referencing this script, the value is optional
   */
  String id() default "";

  /**
   * @return true when the script should be inserted as a header, otherwise it would be as a footer.
   */
  boolean header() default true;

  /**
   * @return the value for resolving the script
   */
  String value();

  /**
   * @return the minified version of the script
   */
  String minified() default "";

  /**
   * @return the script dependencies, i.e the script that are needed by this asset
   */
  String[] depends() default {};

  /**
   * @return the asset location
   */
  AssetLocation location() default AssetLocation.APPLICATION;

  /**
   * @return the <code>max-age</code> cache control headers for this script asset
   */
  int maxAge() default -1;
}
