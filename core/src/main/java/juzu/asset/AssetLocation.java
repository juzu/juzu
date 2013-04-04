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

package juzu.asset;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public enum AssetLocation {

  /**
   * An external asset, not managed by Juzu.
   */
  URL,

  /**
   * A server served asset, server assets URI is managed by Juzu and is computed relative to the server
   * deployment. For instance, for a web application, the asset is located in the web application archive.
   */
  SERVER,

  /**
   * An application served asset.
   */
  APPLICATION;

  public static AssetLocation safeValueOf(String name) {
    if (name != null) {
      try {
        return valueOf(name);
      }
      catch (IllegalArgumentException e) {
        // Should log as warning ?
      }
    }
    return null;
  }
}
