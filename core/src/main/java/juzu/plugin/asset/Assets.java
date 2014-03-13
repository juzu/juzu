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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A container for asset declarations.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface Assets {

  /**
   * The contained assets.
   *
   * @return a list of assets
   */
  Asset[] value() default {};

  /**
   * The default asset location used by the contained assets when no location
   * is explicitly defined.
   *
   * @return the default asset location
   */
  AssetLocation location() default AssetLocation.APPLICATION;

  /**
   * Defines <code>max-age</code> cache control headers for assets, the default value is 1 hour.
   *
   * @return the max age
   */
  int maxAge() default 3600;

}
